package de.mephisto.vpin.ui.preferences;

import de.mephisto.vpin.commons.fx.Debouncer;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.preferences.ServerSettings;
import de.mephisto.vpin.ui.Studio;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class PinVolPreferencesController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(PinVolPreferencesController.class);
  private final Debouncer debouncer = new Debouncer();

  @FXML
  private CheckBox toggleAutoStart;

  @FXML
  private Button openBtn;

  @FXML
  private Button restartBtn;

  @FXML
  private Button stopBtn;

  @FXML
  private Spinner<Integer> volumeSpinner;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    ServerSettings serverSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.SERVER_SETTINGS, ServerSettings.class);

    openBtn.setDisable(!client.getSystemService().isLocal());
    stopBtn.setDisable(!client.getPinVolService().isRunning());

    toggleAutoStart.setSelected(client.getPinVolService().isAutoStartEnabled());
    toggleAutoStart.selectedProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observableValue, Boolean aBoolean, Boolean t1) {
        client.getPinVolService().toggleAutoStart();
      }
    });

    SpinnerValueFactory.IntegerSpinnerValueFactory factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100, serverSettings.getVolume());
    volumeSpinner.setValueFactory(factory);
    volumeSpinner.valueProperty().addListener((observable, oldValue, newValue) -> {
      debouncer.debounce("volume", () -> {
        serverSettings.setVolume(newValue);
        client.getPreferenceService().setJsonPreference(PreferenceNames.SERVER_SETTINGS, serverSettings);
      }, 300);
    });
  }

  @FXML
  private void onRestart() {
    restartBtn.setDisable(true);
    Platform.runLater(() -> {
      stopBtn.setDisable(!client.getPinVolService().restart());
      stopBtn.setDisable(false);
      restartBtn.setDisable(false);
    });
  }

  @FXML
  private void onStop() {
    stopBtn.setDisable(!client.getPinVolService().kill());
    stopBtn.setDisable(true);
  }

  @FXML
  private void onVolumeApply() {
    client.getPinVolService().setVolume();
  }

  @FXML
  private void onLink() {
    Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
    if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
      try {
        desktop.browse(new URI("http://mjrnet.org/pinscape/PinVol.html"));
      } catch (Exception e) {
        LOG.error("Failed to open link: " + e.getMessage());
      }
    }
  }

  @FXML
  private void onOpen() {
    Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
    if (desktop != null && desktop.isSupported(Desktop.Action.OPEN)) {
      try {
        boolean running = client.getPinVolService().isRunning();
        if (running) {
          Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "PinVol Running", "The \"PinVol.exe\" is currently running. To open the UI, the process will be terminated.",
            "The process has to be restarted afterwards.", "Kill Process");
          if (result.isPresent() && result.get().equals(ButtonType.OK)) {
            client.getPinVolService().kill();
          }
          else {
            return;
          }
        }

        File file = new File("resources", "PinVol.exe");
        if (!file.exists()) {
          WidgetFactory.showAlert(Studio.stage, "Did not find PinVol.exe", "The exe file " + file.getAbsolutePath() + " was not found.");
        }
        else {
          desktop.open(file);
        }
      } catch (Exception e) {
        LOG.error("Failed to open Mame Setup: " + e.getMessage(), e);
      }
    }
  }
}
