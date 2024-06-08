package de.mephisto.vpin.server.keyevent;

import de.mephisto.vpin.commons.fx.ServerFX;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.preferences.PauseMenuSettings;
import de.mephisto.vpin.server.VPinStudioServerTray;
import de.mephisto.vpin.server.jobs.JobQueue;
import de.mephisto.vpin.server.popper.PopperService;
import de.mephisto.vpin.server.popper.PopperStatusChangeListener;
import de.mephisto.vpin.server.popper.TableStatusChangedEvent;
import de.mephisto.vpin.server.preferences.PreferenceChangedListener;
import de.mephisto.vpin.server.preferences.PreferencesService;
import de.mephisto.vpin.server.system.SystemService;
import de.mephisto.vpin.server.util.KeyChecker;
import javafx.application.Platform;
import org.apache.commons.lang3.StringUtils;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

@Service
public class KeyEventService implements InitializingBean, NativeKeyListener, PopperStatusChangeListener, PreferenceChangedListener {
  private final static Logger LOG = LoggerFactory.getLogger(KeyEventService.class);

  @Autowired
  private PopperService popperService;

  @Autowired
  private OverlayClientImpl overlayClient;

  @Autowired
  private PreferencesService preferencesService;

  @Autowired
  private SystemService systemService;

  @Autowired
  private JobQueue queue;

  private boolean overlayVisible;
  private ShutdownThread shutdownThread;
  private boolean launchOverlayOnStartup = false;
  private String overlayKey;
  private String resetKey;

  private Map<String, Long> timingMap = new ConcurrentHashMap<>();
  private PauseMenuSettings pauseMenuSettings;

  @Override
  public void nativeKeyTyped(NativeKeyEvent nativeKeyEvent) {

  }

  @Override
  public void nativeKeyPressed(NativeKeyEvent nativeKeyEvent) {
    shutdownThread.notifyKeyEvent();
    handleKeyEvent(nativeKeyEvent);
  }

  private void handleKeyEvent(NativeKeyEvent nativeKeyEvent) {
    if (isEventDebounced(nativeKeyEvent)) {
      return;
    }

    //always hide the overlay on any key press
    if (overlayVisible) {
      LOG.info("Hiding overlay since key '" + nativeKeyEvent.getKeyChar() + "' was pressed.");
      this.overlayVisible = false;
      Platform.runLater(() -> {
        ServerFX.getInstance().showOverlay(false);
      });
      return;
    }

    boolean showPauseInsteadOfOverlay = pauseMenuSettings.isUseOverlayKey();
    if (!StringUtils.isEmpty(overlayKey)) {
      KeyChecker keyChecker = new KeyChecker(overlayKey);
      if (keyChecker.matches(nativeKeyEvent) || (showPauseInsteadOfOverlay && nativeKeyEvent.getRawCode() == pauseMenuSettings.getCustomLaunchKey())) {
        List<ProcessHandle> processes = systemService.getProcesses();
        boolean vpxRunning = systemService.isVPXRunning(processes);
        if (showPauseInsteadOfOverlay && vpxRunning) {
          LOG.info("Toggle pause menu show (Key " + overlayKey + ")");
          ServerFX.getInstance().togglePauseMenu();
          return;
        }

        if (systemService.isPopperMenuRunning(processes)) {
          this.overlayVisible = !overlayVisible;
          Platform.runLater(() -> {
            LOG.info("Toggle pause menu show (Key " + overlayKey + "), was visible: " + !overlayVisible);
            ServerFX.getInstance().showOverlay(overlayVisible);
          });
          return;
        }
      }
    }

    String pauseKey = pauseMenuSettings.getKey();
    if ((!StringUtils.isEmpty(pauseKey) || pauseMenuSettings.getCustomLaunchKey() > 0) && !showPauseInsteadOfOverlay) {
      KeyChecker keyChecker = new KeyChecker(pauseKey);
      if (keyChecker.matches(nativeKeyEvent) || nativeKeyEvent.getRawCode() == pauseMenuSettings.getCustomLaunchKey()) {
        boolean vpxRunning = systemService.isVPXRunning();
        if (vpxRunning) {
          ServerFX.getInstance().togglePauseMenu();
        }
        else {
          ServerFX.getInstance().exitPauseMenu();
        }
        return;
      }
    }

    if (!StringUtils.isEmpty(resetKey)) {
      KeyChecker keyChecker = new KeyChecker(resetKey);
      if (keyChecker.matches(nativeKeyEvent)) {
        new Thread(() -> {
          systemService.restartPopper();
          popperService.notifyPopperRestart();
        }).start();
      }
    }
  }

  private synchronized boolean isEventDebounced(NativeKeyEvent nativeKeyEvent) {
    long inputDeboundeMs = pauseMenuSettings.getInputDebounceMs();
    String pauseKey = pauseMenuSettings.getKey();

    if (inputDeboundeMs > 0) {
      if (!StringUtils.isEmpty(overlayKey)) {
        KeyChecker keyChecker = new KeyChecker(overlayKey);
        if (keyChecker.matches(nativeKeyEvent)) {
          if (timingMap.containsKey(overlayKey) && (System.currentTimeMillis() - timingMap.get(overlayKey)) < inputDeboundeMs) {
            LOG.info("Debouncer: Skipped overlay key event, because it event within debounce range.");
            return true;
          }
          timingMap.put(overlayKey, System.currentTimeMillis());
          return false;
        }
      }

      if (!StringUtils.isEmpty(pauseKey)) {
        KeyChecker keyChecker = new KeyChecker(pauseKey);
        if (keyChecker.matches(nativeKeyEvent)) {
          if (timingMap.containsKey(pauseKey) && (System.currentTimeMillis() - timingMap.get(pauseKey)) < inputDeboundeMs) {
            LOG.info("Debouncer: Skipped pause key event, because it event within debounce range.");
            return true;
          }
          timingMap.put(pauseKey, System.currentTimeMillis());
          return false;
        }
      }
    }
    return false;
  }

  @Override
  public void nativeKeyReleased(NativeKeyEvent nativeKeyEvent) {

  }

  @Override
  public void popperLaunched() {
    Platform.runLater(() -> {
      if (this.launchOverlayOnStartup) {
        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          //ignore
        }
        this.overlayVisible = true;
        ServerFX.getInstance().showOverlay(overlayVisible);
      }
    });
  }

  @Override
  public void tableLaunched(TableStatusChangedEvent event) {

  }

  @Override
  public void tableExited(TableStatusChangedEvent event) {

  }

  @Override
  public void popperExited() {

  }

  @Override
  public void popperRestarted() {

  }

  public void resetShutdownTimer() {
    shutdownThread.reset();
  }

  @Override
  public void afterPropertiesSet() throws NativeHookException {
    new Thread(() -> {
      ServerFX.main(new String[]{});
      LOG.info("Overlay listener started.");
    }).start();

    shutdownThread = new ShutdownThread(preferencesService, queue);
    shutdownThread.start();

    ServerFX.client = overlayClient;
    ServerFX.waitForOverlay();
    LOG.info("Finished initialization of OverlayWindowFX");

    new VPinStudioServerTray();
    LOG.info("Application tray created.");

    try {
      InetAddress localHost = InetAddress.getLocalHost();
      LOG.info("Server Address: " + localHost.getHostName() + "/" + localHost.getHostAddress());
    } catch (UnknownHostException e) {
      //
    }

    preferenceChanged(PreferenceNames.SHOW_OVERLAY_ON_STARTUP, null, null);
    preferenceChanged(PreferenceNames.OVERLAY_KEY, null, null);
    preferenceChanged(PreferenceNames.RESET_KEY, null, null);
    preferenceChanged(PreferenceNames.PAUSE_MENU_SETTINGS, null, null);

    preferencesService.addChangeListener(this);

    boolean pinUPRunning = popperService.isPinUPRunning();
    if (pinUPRunning) {
      popperLaunched();
    }
    else {
      LOG.info("Added VPin service popper status listener.");
      popperService.addPopperStatusChangeListener(this);
    }

    GlobalScreen.registerNativeHook();
    java.util.logging.Logger logger = java.util.logging.Logger.getLogger(GlobalScreen.class.getPackage().getName());
    logger.setLevel(Level.OFF);
    logger.setUseParentHandlers(false);
    GlobalScreen.addNativeKeyListener(this);

    LOG.info("Server startup finished, running version is " + systemService.getVersion());
  }

  @Override
  public void preferenceChanged(String propertyName, Object oldValue, Object newValue) {
    try {
      switch (propertyName) {
        case PreferenceNames.SHOW_OVERLAY_ON_STARTUP: {
          String startupLaunch = (String) preferencesService.getPreferenceValue(PreferenceNames.SHOW_OVERLAY_ON_STARTUP);
          this.launchOverlayOnStartup = !StringUtils.isEmpty(startupLaunch) && Boolean.parseBoolean(startupLaunch);
          LOG.info("Show overlay on startup: " + this.launchOverlayOnStartup);
          break;
        }
        case PreferenceNames.OVERLAY_KEY: {
          overlayKey = (String) preferencesService.getPreferenceValue(PreferenceNames.OVERLAY_KEY);
          LOG.info("Overlay key has been updated to: " + overlayKey);
          break;
        }
        case PreferenceNames.RESET_KEY: {
          resetKey = (String) preferencesService.getPreferenceValue(PreferenceNames.RESET_KEY);
          LOG.info("Reset key has been updated to: " + resetKey);
          break;
        }
        case PreferenceNames.PAUSE_MENU_SETTINGS: {
          pauseMenuSettings = preferencesService.getJsonPreference(PreferenceNames.PAUSE_MENU_SETTINGS, PauseMenuSettings.class);
          LOG.info("Pause key has been updated.");
          break;
        }
      }
    } catch (Exception e) {
      LOG.error("Error updating KeyEventService settings: " + e.getMessage(), e);
    }
  }
}
