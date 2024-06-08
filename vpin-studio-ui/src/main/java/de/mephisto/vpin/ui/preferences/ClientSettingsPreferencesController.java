package de.mephisto.vpin.ui.preferences;

import de.mephisto.vpin.commons.fx.Debouncer;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.games.GameEmulatorRepresentation;
import de.mephisto.vpin.restclient.preferences.UISettings;
import de.mephisto.vpin.ui.PreferencesController;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.util.SystemUtil;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.Studio.stage;

public class ClientSettingsPreferencesController implements Initializable, ChangeListener<Boolean> {
  private final static Logger LOG = LoggerFactory.getLogger(ClientSettingsPreferencesController.class);

  @FXML
  private VBox emulatorList;

  @FXML
  private TextField winNetworkShare;

  @FXML
  private Button winNetworkShareTestBtn;

  @FXML
  private Label winNetworkShareStatusLabel;

  @FXML
  private CheckBox uiShowVersion;

  @FXML
  private CheckBox uiShowVPSUpdates;

  @FXML
  private CheckBox autoEditCheckbox;

  @FXML
  private CheckBox vpsAltSound;
  @FXML
  private CheckBox vpsAltColor;
  @FXML
  private CheckBox vpsBackglass;
  @FXML
  private CheckBox vpsPOV;
  @FXML
  private CheckBox vpsPUPPack;
  @FXML
  private CheckBox vpsRom;
  @FXML
  private CheckBox vpsSound;
  @FXML
  private CheckBox vpsToppper;
  @FXML
  private CheckBox vpsTutorial;
  @FXML
  private CheckBox vpsWheel;

  public static Debouncer debouncer = new Debouncer();
  private String networkShareTestPath;
  private UISettings uiSettings;


  @FXML
  private void onWinShareTest() {
    SystemUtil.publicUrl = winNetworkShare.getText();
    SystemUtil.openFolder(new File(networkShareTestPath));
  }

  @FXML
  private void onHideReset() {
    Optional<ButtonType> result = WidgetFactory.showConfirmation(stage, "Reset \"Do not show again\" flags?", "All previously hidden dialogs or panels will be shown again.");
    if (result.isPresent() && result.get().equals(ButtonType.OK)) {
      UISettings uiSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.UI_SETTINGS, UISettings.class);

      uiSettings.setHideComponentWarning(false);
      uiSettings.setHideDismissConfirmations(false);
      uiSettings.setHideVPXStartInfo(false);

      client.getPreferenceService().setJsonPreference(PreferenceNames.UI_SETTINGS, uiSettings);
      EventManager.getInstance().notifyPreferenceChanged(PreferenceType.uiSettings);
    }
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    networkShareTestPath = client.getPinUPPopperService().getDefaultGameEmulator().getInstallationDirectory();

    uiSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.UI_SETTINGS, UISettings.class);

    uiShowVersion.setSelected(!uiSettings.isHideVersions());
    uiShowVersion.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      uiSettings.setHideVersions(!t1);
      PreferencesController.markDirty(PreferenceType.uiSettings);
      client.getPreferenceService().setJsonPreference(PreferenceNames.UI_SETTINGS, uiSettings);
    });

    boolean disabled = uiSettings.isHideVPSUpdates();
    vpsAltSound.setDisable(disabled);
    vpsAltSound.setSelected(uiSettings.isVpsAltSound());
    vpsAltColor.setDisable(disabled);
    vpsAltColor.setSelected(uiSettings.isVpsAltColor());
    vpsBackglass.setDisable(disabled);
    vpsBackglass.setSelected(uiSettings.isVpsBackglass());
    vpsPOV.setDisable(disabled);
    vpsPOV.setSelected(uiSettings.isVpsPOV());
    vpsPUPPack.setDisable(disabled);
    vpsPUPPack.setSelected(uiSettings.isVpsPUPPack());
    vpsRom.setDisable(disabled);
    vpsRom.setSelected(uiSettings.isVpsRom());
    vpsSound.setDisable(disabled);
    vpsSound.setSelected(uiSettings.isVpsSound());
    vpsToppper.setDisable(disabled);
    vpsToppper.setSelected(uiSettings.isVpsToppper());
    vpsTutorial.setDisable(disabled);
    vpsTutorial.setSelected(uiSettings.isVpsTutorial());
    vpsWheel.setDisable(disabled);
    vpsWheel.setSelected(uiSettings.isVpsWheel());

    vpsAltSound.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      uiSettings.setVpsAltSound(t1);
      PreferencesController.markDirty(PreferenceType.uiSettings);
      client.getPreferenceService().setJsonPreference(PreferenceNames.UI_SETTINGS, uiSettings);
    });
    vpsAltColor.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      uiSettings.setVpsAltColor(t1);
      PreferencesController.markDirty(PreferenceType.uiSettings);
      client.getPreferenceService().setJsonPreference(PreferenceNames.UI_SETTINGS, uiSettings);
    });
    vpsBackglass.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      uiSettings.setVpsBackglass(t1);
      PreferencesController.markDirty(PreferenceType.uiSettings);
      client.getPreferenceService().setJsonPreference(PreferenceNames.UI_SETTINGS, uiSettings);
    });
    vpsPOV.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      uiSettings.setVpsPOV(t1);
      PreferencesController.markDirty(PreferenceType.uiSettings);
      client.getPreferenceService().setJsonPreference(PreferenceNames.UI_SETTINGS, uiSettings);
    });
    vpsPUPPack.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      uiSettings.setVpsPUPPack(t1);
      PreferencesController.markDirty(PreferenceType.uiSettings);
      client.getPreferenceService().setJsonPreference(PreferenceNames.UI_SETTINGS, uiSettings);
    });
    vpsRom.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      uiSettings.setVpsRom(t1);
      PreferencesController.markDirty(PreferenceType.uiSettings);
      client.getPreferenceService().setJsonPreference(PreferenceNames.UI_SETTINGS, uiSettings);
    });
    vpsSound.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      uiSettings.setVpsSound(t1);
      PreferencesController.markDirty(PreferenceType.uiSettings);
      client.getPreferenceService().setJsonPreference(PreferenceNames.UI_SETTINGS, uiSettings);
    });
    vpsToppper.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      uiSettings.setVpsToppper(t1);
      PreferencesController.markDirty(PreferenceType.uiSettings);
      client.getPreferenceService().setJsonPreference(PreferenceNames.UI_SETTINGS, uiSettings);
    });
    vpsTutorial.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      uiSettings.setVpsTutorial(t1);
      PreferencesController.markDirty(PreferenceType.uiSettings);
      client.getPreferenceService().setJsonPreference(PreferenceNames.UI_SETTINGS, uiSettings);
    });
    vpsWheel.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      uiSettings.setVpsWheel(t1);
      PreferencesController.markDirty(PreferenceType.uiSettings);
      client.getPreferenceService().setJsonPreference(PreferenceNames.UI_SETTINGS, uiSettings);
    });

    uiShowVPSUpdates.setSelected(!uiSettings.isHideVPSUpdates());
    uiShowVPSUpdates.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      uiSettings.setHideVPSUpdates(!t1);
      PreferencesController.markDirty(PreferenceType.uiSettings);
      client.getPreferenceService().setJsonPreference(PreferenceNames.UI_SETTINGS, uiSettings);

      boolean disabledSelection = !t1;
      vpsAltSound.setDisable(disabledSelection);
      vpsAltColor.setDisable(disabledSelection);
      vpsBackglass.setDisable(disabledSelection);
      vpsPOV.setDisable(disabledSelection);
      vpsPUPPack.setDisable(disabledSelection);
      vpsRom.setDisable(disabledSelection);
      vpsSound.setDisable(disabledSelection);
      vpsToppper.setDisable(disabledSelection);
      vpsTutorial.setDisable(disabledSelection);
      vpsWheel.setDisable(disabledSelection);
    });

    autoEditCheckbox.setSelected(uiSettings.isAutoEditTableData());
    autoEditCheckbox.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      uiSettings.setAutoEditTableData(t1);
      PreferencesController.markDirty(PreferenceType.uiSettings);
      client.getPreferenceService().setJsonPreference(PreferenceNames.UI_SETTINGS, uiSettings);
    });

    winNetworkShare.setText(uiSettings.getWinNetworkShare());
    winNetworkShare.setDisable(!SystemUtil.isWindows());
    winNetworkShareStatusLabel.setVisible(SystemUtil.isWindows() && !StringUtils.isEmpty(winNetworkShare.getText()));
    winNetworkShare.textProperty().addListener(new ChangeListener<String>() {
      @Override
      public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
        debouncer.debounce("winNetworkShare", () -> {
          uiSettings.setWinNetworkShare(newValue);

          boolean visible = SystemUtil.isWindows() && !StringUtils.isEmpty(newValue);
          winNetworkShareStatusLabel.setVisible(visible);
          refreshNetworkStatusLabel(newValue);
          client.getPreferenceService().setJsonPreference(PreferenceNames.UI_SETTINGS, uiSettings);
        }, 300);
      }
    });
    winNetworkShareTestBtn.setDisable(!SystemUtil.isWindows());
    refreshNetworkStatusLabel(uiSettings.getWinNetworkShare());

    List<GameEmulatorRepresentation> gameEmulators = Studio.client.getPinUPPopperService().getGameEmulators();
    List<GameEmulatorRepresentation> backglassGameEmulators = Studio.client.getPinUPPopperService().getBackglassGameEmulators();
    for (GameEmulatorRepresentation gameEmulator : gameEmulators) {
      CheckBox checkBox = new CheckBox(gameEmulator.getName());
      checkBox.setUserData(gameEmulator);
      checkBox.setDisable(gameEmulator.isVpxEmulator() || backglassGameEmulators.contains(gameEmulator));
      checkBox.setSelected(checkBox.isDisabled() || !uiSettings.getIgnoredEmulatorIds().contains(gameEmulator.getId()));
      checkBox.getStyleClass().add("preference-checkbox");
      checkBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
        @Override
        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
          if (newValue) {
            uiSettings.getIgnoredEmulatorIds().remove(Integer.valueOf(gameEmulator.getId()));
          }
          else {
            if (!uiSettings.getIgnoredEmulatorIds().contains(Integer.valueOf(gameEmulator.getId()))) {
              uiSettings.getIgnoredEmulatorIds().add(Integer.valueOf(gameEmulator.getId()));
            }
          }
          PreferencesController.markDirty(PreferenceType.serverSettings);
          client.getPreferenceService().setJsonPreference(PreferenceNames.UI_SETTINGS, uiSettings);
        }
      });

      emulatorList.getChildren().add(checkBox);
    }
  }

  private void refreshNetworkStatusLabel(String newValue) {
    winNetworkShareTestBtn.setDisable(true);
    Platform.runLater(() -> {
      String path = SystemUtil.resolveNetworkPath(newValue, networkShareTestPath);
      if (StringUtils.isEmpty(newValue) || !SystemUtil.isWindows()) {
        winNetworkShareStatusLabel.setVisible(false);
        return;
      }

      if (!newValue.startsWith("\\\\")) {
        winNetworkShareStatusLabel.setText("Network path must with \"\\\\\".");
      }
      else if (path == null) {
        winNetworkShareStatusLabel.setText("No matching path with VPX installation found, using test folder \"" + networkShareTestPath + "\"");
      }
      else {
        winNetworkShareStatusLabel.setText("Test Folder: " + path);
        winNetworkShareTestBtn.setDisable(false);
      }
    });
  }

  @Override
  public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
//    Node checkBox = (Node) observable;
//    GameEmulatorRepresentation emulatorRepresentation = (GameEmulatorRepresentation) checkBox.getUserData();

//    if (newValue) {
//      uiSettings.getIgnoredEmulatorIds().remove(emulatorRepresentation.getId());
//    }
//    else {
//      uiSettings.getIgnoredEmulatorIds().add(emulatorRepresentation.getId());
//    }
//    PreferencesController.markDirty(PreferenceType.serverSettings);
//    client.getPreferenceService().setJsonPreference(PreferenceNames.UI_SETTINGS, uiSettings);
  }
}
