package de.mephisto.vpin.ui.preferences;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.mame.MameOptions;
import de.mephisto.vpin.ui.PreferencesController;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.tables.TablesSidebarMameController;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class MamePreferencesController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(MamePreferencesController.class);

  @FXML
  private CheckBox skipPinballStartupTest;

  @FXML
  private CheckBox useSound;

  @FXML
  private CheckBox useSamples;

  @FXML
  private CheckBox compactDisplay;

  @FXML
  private CheckBox doubleDisplaySize;

  @FXML
  private CheckBox ignoreRomCrcError;

  @FXML
  private CheckBox cabinetMode;

  @FXML
  private CheckBox showDmd;

  @FXML
  private CheckBox useExternalDmd;

  @FXML
  private CheckBox colorizeDmd;

  @FXML
  private ComboBox<TablesSidebarMameController.SoundMode> soundModeCombo;

  @FXML
  private CheckBox forceStereo;

  private void saveOptions() {
    MameOptions options = new MameOptions();
    options.setRom(MameOptions.DEFAULT_KEY);

    options.setIgnoreRomCrcError(ignoreRomCrcError.isSelected());
    options.setSkipPinballStartupTest(skipPinballStartupTest.isSelected());
    options.setUseSamples(useSamples.isSelected());
    options.setUseSound(useSound.isSelected());
    options.setCompactDisplay(compactDisplay.isSelected());
    options.setDoubleDisplaySize(doubleDisplaySize.isSelected());
    options.setSoundMode(soundModeCombo.getValue().getId());
    options.setShowDmd(showDmd.isSelected());
    options.setUseExternalDmd(useExternalDmd.isSelected());
    options.setCabinetMode(cabinetMode.isSelected());
    options.setColorizeDmd(colorizeDmd.isSelected());
    options.setSoundMode(soundModeCombo.getValue().getId());
    options.setForceStereo(forceStereo.isSelected());

    try {
      client.getMameService().saveOptions(options);
    } catch (Exception e) {
      LOG.error("Failed to save mame settings: " + e.getMessage(), e);
      WidgetFactory.showAlert(Studio.stage, "Error", "Failed to save mame settings: " + e.getMessage());
    }

    PreferencesController.markDirty(PreferenceType.serverSettings);
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    MameOptions options = client.getMameService().getOptions(MameOptions.DEFAULT_KEY);


    skipPinballStartupTest.setSelected(options.isSkipPinballStartupTest());
    skipPinballStartupTest.selectedProperty().addListener((observable, oldValue, newValue) -> saveOptions());
    useSound.setSelected(options.isUseSound());
    useSound.selectedProperty().addListener((observable, oldValue, newValue) -> saveOptions());
    useSamples.setSelected(options.isUseSamples());
    useSamples.selectedProperty().addListener((observable, oldValue, newValue) -> saveOptions());
    compactDisplay.setSelected(options.isUseSamples());
    compactDisplay.selectedProperty().addListener((observable, oldValue, newValue) -> saveOptions());
    doubleDisplaySize.setSelected(options.isUseSamples());
    doubleDisplaySize.selectedProperty().addListener((observable, oldValue, newValue) -> saveOptions());
    ignoreRomCrcError.setSelected(options.isIgnoreRomCrcError());
    ignoreRomCrcError.selectedProperty().addListener((observable, oldValue, newValue) -> saveOptions());
    cabinetMode.setSelected(options.isCabinetMode());
    cabinetMode.selectedProperty().addListener((observable, oldValue, newValue) -> saveOptions());
    showDmd.setSelected(options.isShowDmd());
    showDmd.selectedProperty().addListener((observable, oldValue, newValue) -> saveOptions());
    useExternalDmd.setSelected(options.isUseExternalDmd());
    useExternalDmd.selectedProperty().addListener((observable, oldValue, newValue) -> saveOptions());
    colorizeDmd.setSelected(options.isColorizeDmd());
    colorizeDmd.selectedProperty().addListener((observable, oldValue, newValue) -> saveOptions());
    soundModeCombo.setItems(FXCollections.observableList(TablesSidebarMameController.SOUND_MODES));
    soundModeCombo.setValue(TablesSidebarMameController.SOUND_MODES.get(options.getSoundMode()));
    soundModeCombo.valueProperty().addListener((observable, oldValue, newValue) -> saveOptions());
    forceStereo.setSelected(options.isForceStereo());
    forceStereo.selectedProperty().addListener((observable, oldValue, newValue) -> saveOptions());
  }
}
