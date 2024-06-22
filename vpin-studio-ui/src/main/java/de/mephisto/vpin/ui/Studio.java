package de.mephisto.vpin.ui;

import de.mephisto.vpin.commons.fx.Features;
import de.mephisto.vpin.commons.fx.ServerFX;
import de.mephisto.vpin.commons.utils.LocalUISettings;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.connectors.mania.VPinManiaClient;
import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.client.VPinStudioClientErrorHandler;
import de.mephisto.vpin.restclient.system.SystemSummary;
import de.mephisto.vpin.restclient.tournaments.TournamentConfig;
import de.mephisto.vpin.ui.launcher.LauncherController;
import de.mephisto.vpin.ui.tables.TableReloadProgressModel;
import de.mephisto.vpin.ui.tables.vbsedit.VBSManager;
import de.mephisto.vpin.ui.util.Dialogs;
import de.mephisto.vpin.ui.util.FXResizeHelper;
import de.mephisto.vpin.ui.util.ProgressDialog;
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.SevenZipNativeInitializationException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

public class Studio extends Application {
  private final static Logger LOG = LoggerFactory.getLogger(Studio.class);
  public static final double SCALING_DIFF = 0.05;

  public static Stage stage;

  public static VPinStudioClient client;
  public static VPinManiaClient maniaClient;

  private static HostServices hostServices;

  private static double scaling = 1;

  public static void main(String[] args) {
    launch(args);
  }

  private static VPinStudioClientErrorHandler errorHandler;

  @Override
  public void start(Stage stage) throws IOException {
    Studio.stage = stage;
    Studio.hostServices = getHostServices();

    Locale.setDefault(Locale.ENGLISH);
    StudioUpdatePreProcessing.execute();

    Studio.errorHandler = e -> {
      client.setErrorHandler(null);
      Platform.runLater(() -> {
        Studio.stage.close();
        NavigationController.refreshControllerCache();
        NavigationController.refreshViewCache();

        Studio.loadLauncher(new Stage());
        WidgetFactory.showAlert(stage, "Server Connection Failed", "You have been disconnected from the server.");
      });
    };

    //replace the OverlayFX client with the Studio one
    Studio.client = new VPinStudioClient("localhost");
    ServerFX.client = Studio.client;

    String version = client.getSystemService().getVersion();
    if (!StringUtils.isEmpty(version)) {
      loadStudio(stage, Studio.client);
    }
    else {
      loadLauncher(stage);
    }
  }

  public static HostServices getStudioHostServices() {
    return hostServices;
  }

  public static void loadLauncher(Stage stage) {
    try {
      Studio.stage = stage;
      Rectangle2D screenBounds = Screen.getPrimary().getBounds();
      FXMLLoader loader = new FXMLLoader(LauncherController.class.getResource("scene-launcher.fxml"));
      Parent root = loader.load();


      Scene scene = new Scene(root, 800, 500);
      scene.setFill(Paint.valueOf("#212529"));
      stage.setTitle("VPin Studio Launcher");
      stage.getIcons().add(new Image(Studio.class.getResourceAsStream("logo-64.png")));
      stage.setScene(scene);
      stage.initStyle(StageStyle.UNDECORATED);
      stage.setX((screenBounds.getWidth() / 2) - (800 / 2));
      stage.setY((screenBounds.getHeight() / 2) - (400 / 2));

      LauncherController controller = loader.getController();
      controller.setStage(stage);
      stage.show();
    }
    catch (IOException e) {
      LOG.error("Failed to load launcher: " + e.getMessage(), e);
    }
  }

  public static void loadStudio(Stage stage, VPinStudioClient client) {
    try {
      try {
        File sevenZipTempFolder = new File(System.getProperty("java.io.tmpdir"), "sevenZip/");
        sevenZipTempFolder.mkdirs();
        SevenZip.initSevenZipFromPlatformJAR(sevenZipTempFolder);
      }
      catch (Exception e) {
        LOG.error("Failed to initialize SevenZip (.rar support): " + e.getMessage(), e);
      }

      SystemSummary systemSummary = client.getSystemService().getSystemSummary();
      if (!systemSummary.isPopper15()) {
        WidgetFactory.showAlert(new Stage(), "Invalid PinUP Popper version.", "Please install version 1.5 or higher to use VPin Studio.");
        System.exit(0);
      }

      Stage splash = createSplash();

      Platform.runLater(() -> {
        Studio.stage = stage;
        Rectangle2D screenBounds = Screen.getPrimary().getBounds();

        if (screenBounds.getWidth() > screenBounds.getHeight()) {
          LOG.info("Window Mode: Landscape");
        }
        else {
          LOG.info("Window Mode: Portrait");
        }

        //replace the OverlayFX client with the Studio one
        Studio.client = client;
        createManiaClient();
        ServerFX.client = Studio.client;

        List<Integer> unknownGameIds = client.getGameService().getUnknownGameIds();
        if (!unknownGameIds.isEmpty()) {
          LOG.info("Initial scan of " + unknownGameIds.size() + " unknown tables.");
          ProgressDialog.createProgressDialog(new TableReloadProgressModel(unknownGameIds));
        }

        //force pre-caching, this way, the table overview does not need to execute single GET requests
        Studio.client.getVpsService().invalidateAll();

        FXMLLoader loader = new FXMLLoader(Studio.class.getResource("scene-root.fxml"));
        Parent root = null;
        try {
          root = loader.load();
        }
        catch (IOException e) {
          throw new RuntimeException(e);
        }

        Rectangle position = LocalUISettings.getPosition();

        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
        double width = bounds.getWidth() - (bounds.getWidth() * 10 / 100);
        double height = bounds.getHeight() - (bounds.getHeight() * 10 / 100);
        if (position.getWidth() != -1) {
          width = position.getWidth();
          height = position.getHeight();
        }

        Scene scene = new Scene(root, width, height);
        scene.setFill(Paint.valueOf("#212529"));
        stage.getIcons().add(new Image(Studio.class.getResourceAsStream("logo-128.png")));
        stage.setScene(scene);
        stage.setMinWidth(1280);
        stage.setMinHeight(950);
        stage.setResizable(true);
        stage.initStyle(StageStyle.UNDECORATED);


        if (position.getX() != -1) {
          stage.setX(position.getX());
          stage.setY(position.getY());
        }
        else {
          stage.setX((screenBounds.getWidth() / 2) - (width / 2));
          stage.setY((screenBounds.getHeight() / 2) - (height / 2));
        }

//      ResizeHelper.addResizeListener(stage);
        FXResizeHelper fxResizeHelper = new FXResizeHelper(stage, 30, 6);
        stage.setUserData(fxResizeHelper);

        scene.addEventFilter(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
          public void handle(KeyEvent ke) {
            if (ke.getCode() == KeyCode.U && ke.isAltDown() && ke.isControlDown()) {
              Dialogs.openUpdateInfoDialog(client.getSystemService().getVersion(), true);
              ke.consume();
            }
            if (ke.getCode() == KeyCode.MINUS && ke.isAltDown() && ke.isControlDown()) {
              Parent r = stage.getScene().getRoot();
              if (scaling < SCALING_DIFF) {
                return;
              }
              scaling = scaling - SCALING_DIFF;
              stage.setWidth(stage.getWidth() * scaling);
              stage.setHeight(stage.getHeight() * scaling);

              r.setScaleX(scaling);
              r.setScaleY(scaling);
              ke.consume();
            }
            if (ke.getCode() == KeyCode.PLUS && ke.isAltDown() && ke.isControlDown()) {
              Parent r = stage.getScene().getRoot();
              if (scaling > (1 - SCALING_DIFF)) {
                return;
              }
              scaling = scaling + SCALING_DIFF;
              r.setScaleX(scaling);
              r.setScaleY(scaling);
              stage.setWidth(stage.getWidth() * scaling);
              stage.setHeight(stage.getHeight() * scaling);
              ke.consume();
            }
            if (ke.getCode() == KeyCode.H && ke.isAltDown() && ke.isControlDown()) {
              Parent r = stage.getScene().getRoot();
              scaling = 1;
              stage.setWidth(1920);
              stage.setHeight(1080);
              r.setScaleX(1);
              r.setScaleY(1);
              ke.consume();
            }
            if (ke.getCode() == KeyCode.W && ke.isAltDown() && ke.isControlDown()) {
              Parent r = stage.getScene().getRoot();
              scaling = 1;
              stage.setWidth(2560);
              stage.setHeight(1440);
              r.setScaleX(1);
              r.setScaleY(1);
              ke.consume();
            }
            if (ke.getCode() == KeyCode.C && ke.isAltDown() && ke.isControlDown()) {
              Parent r = stage.getScene().getRoot();
              scaling = 1;
              stage.setWidth(1024);
              stage.setHeight(600);
              r.setScaleX(1);
              r.setScaleY(1);
              ke.consume();
            }
          }
        });

        client.setErrorHandler(errorHandler);
        stage.show();
        splash.hide();

        //launch VPSMonitor
        VBSManager.getInstance();
      });

    }
    catch (Exception e) {
      LOG.error("Failed to load Studio: " + e.getMessage(), e);
    }
  }

  private static Stage createSplash() throws Exception {
    FXMLLoader loader = new FXMLLoader(SplashScreenController.class.getResource("scene-splash.fxml"));
    StackPane root = loader.load();
    Scene scene = new Scene(root, 600, 400);
    Rectangle2D screenBounds = Screen.getPrimary().getBounds();

    Stage stage = new Stage(StageStyle.UNDECORATED);
    stage.getIcons().add(new Image(Studio.class.getResourceAsStream("logo-64.png")));
    stage.setScene(scene);
    stage.setX((screenBounds.getWidth() / 2) - (600 / 2));
    stage.setY((screenBounds.getHeight() / 2) - (400 / 2));
    stage.setResizable(false);
    stage.show();
    return stage;
  }

  private static void createManiaClient() {
    try {
      if (Features.TOURNAMENTS_ENABLED) {
        TournamentConfig config = Studio.client.getTournamentsService().getConfig();
        SystemSummary summary = Studio.client.getSystemService().getSystemSummary();
        Studio.maniaClient = new VPinManiaClient(config.getUrl(), summary.getSystemId());
      }
    }
    catch (Exception e) {
      LOG.error("Failed to create mania client: " + e.getMessage());
    }
  }

  public static String getVersion() {
    try {
      final Properties properties = new Properties();
      InputStream resourceAsStream = Studio.class.getClassLoader().getResourceAsStream("version.properties");
      properties.load(resourceAsStream);
      resourceAsStream.close();
      return properties.getProperty("vpin.studio.version");
    }
    catch (IOException e) {
      LOG.error("Failed to read version number: " + e.getMessage(), e);
    }
    return null;
  }
}