package de.mephisto.vpin.ui.archiving;

import de.mephisto.vpin.commons.utils.FileUtils;
import de.mephisto.vpin.restclient.archiving.ArchiveDescriptorRepresentation;
import de.mephisto.vpin.restclient.archiving.ArchivePackageInfo;
import de.mephisto.vpin.restclient.frontend.TableDetails;
import de.mephisto.vpin.ui.NavigationOptions;
import de.mephisto.vpin.ui.StudioFXController;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Accordion;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.StringUtils;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Optional;
import java.util.ResourceBundle;

public class RepositorySidebarController implements Initializable, StudioFXController {
  private final static Logger LOG = LoggerFactory.getLogger(RepositorySidebarController.class);

  @FXML
  private Accordion repositoryAccordion;

  @FXML
  private TitledPane detailsPane;

  @FXML
  private VBox repositoryAccordionVBox;

  @FXML
  private Label sourceLabel;

  @FXML
  private Label filenameLabel;

  @FXML
  private Label fileSizeLabel;

  @FXML
  private Label lastModifiedLabel;

  @FXML
  private FontIcon directb2sIcon;

  @FXML
  private FontIcon pupPackIcon;

  @FXML
  private FontIcon romIcon;

  @FXML
  private FontIcon cfgIcon;

  @FXML
  private FontIcon popperIcon;

  @FXML
  private FontIcon flexIcon;

  @FXML
  private FontIcon ultraIcon;

  @FXML
  private FontIcon musicIcon;

  @FXML
  private FontIcon altSoundIcon;

  @FXML
  private FontIcon altColorIcon;

  @FXML
  private FontIcon povIcon;

  @FXML
  private FontIcon resIcon;

  @FXML
  private FontIcon highscoreIcon;

  @FXML
  private FontIcon registryIcon;

  @FXML
  private TitledPane manifestPane;

  @FXML
  private Label gameName;

  @FXML
  private Label gameFileName;

  @FXML
  private Label gameDisplayName;

  @FXML
  private Label gameYear;

  @FXML
  private Label romName;

  @FXML
  private Label gameType;

  @FXML
  private Label gameTheme;

  @FXML
  private Label gameVersion;

  @FXML
  private Label manufacturer;

  @FXML
  private Label numberOfPlayers;

  @FXML
  private Label tags;

  @FXML
  private Label category;

  @FXML
  private Label author;

  @FXML
  private Label volume;

  @FXML
  private Label launchCustomVar;

  @FXML
  private Label keepDisplays;

  @FXML
  private Label gameRating;

  @FXML
  private Label dof;

  @FXML
  private Label IPDBNum;

  @FXML
  private Label altRunMode;

  @FXML
  private Label url;

  @FXML
  private Label designedBy;

  @FXML
  private Label notes;

  @Override
  public void onViewActivated(NavigationOptions options) {

  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    repositoryAccordion.managedProperty().bindBidirectional(repositoryAccordion.visibleProperty());
    repositoryAccordion.setExpandedPane(detailsPane);
    setArchiveDescriptor(Optional.empty());
  }

  public void setVisible(boolean b) {
    this.repositoryAccordion.setVisible(b);
  }

  public void setArchiveDescriptor(Optional<ArchiveDescriptorRepresentation> selection) {
    directb2sIcon.setVisible(false);
    pupPackIcon.setVisible(false);
    romIcon.setVisible(false);
    popperIcon.setVisible(false);
    flexIcon.setVisible(false);
    ultraIcon.setVisible(false);
    musicIcon.setVisible(false);
    altSoundIcon.setVisible(false);
    altColorIcon.setVisible(false);
    resIcon.setVisible(false);
    cfgIcon.setVisible(false);
    povIcon.setVisible(false);
    highscoreIcon.setVisible(false);
    registryIcon.setVisible(false);

    filenameLabel.setText("-");
    filenameLabel.setTooltip(null);
    fileSizeLabel.setText("-");
    lastModifiedLabel.setText("-");
    sourceLabel.setText("-");
    sourceLabel.setTooltip(null);

    if (selection.isPresent()) {
      ArchiveDescriptorRepresentation descriptorRepresentation = selection.get();
      filenameLabel.setText(descriptorRepresentation.getFilename());
      filenameLabel.setTooltip(new Tooltip(descriptorRepresentation.getFilename()));
      fileSizeLabel.setText(descriptorRepresentation.getSize() > 0 ? FileUtils.readableFileSize(descriptorRepresentation.getSize()) : "-");
      lastModifiedLabel.setText(SimpleDateFormat.getDateTimeInstance().format(descriptorRepresentation.getCreatedAt()));
      sourceLabel.setText(descriptorRepresentation.getSource().getLocation());
      sourceLabel.setTooltip(new Tooltip(descriptorRepresentation.getSource().getLocation()));

      ArchivePackageInfo packageInfo = descriptorRepresentation.getPackageInfo();

      if (packageInfo != null) {
        directb2sIcon.setVisible(packageInfo.isDirectb2s());
        pupPackIcon.setVisible(packageInfo.isPupPack());
        romIcon.setVisible(packageInfo.isRom());
        resIcon.setVisible(packageInfo.isRes());
        cfgIcon.setVisible(packageInfo.isCfg());
        popperIcon.setVisible(packageInfo.isPopperMedia());
        flexIcon.setVisible(packageInfo.isFlexDMD());
        ultraIcon.setVisible(packageInfo.isUltraDMD());
        musicIcon.setVisible(packageInfo.isMusic());
        altSoundIcon.setVisible(packageInfo.isAltSound());
        altColorIcon.setVisible(packageInfo.isAltColor());
        povIcon.setVisible(packageInfo.isPov());
        highscoreIcon.setVisible(packageInfo.isHighscore());
        registryIcon.setVisible(packageInfo.isRegistryData());
      }

      TableDetails tableDetails = descriptorRepresentation.getTableDetails();
      manifestPane.setVisible(tableDetails != null);
      if (tableDetails == null) {
        tableDetails = new TableDetails();
      }
      gameName.setText(StringUtils.isEmpty(tableDetails.getGameName()) ? "-" : tableDetails.getGameName());
      gameFileName.setText(StringUtils.isEmpty(tableDetails.getGameFileName()) ? "-" : tableDetails.getGameFileName());
      gameDisplayName.setText(StringUtils.isEmpty(tableDetails.getGameDisplayName()) ? "-" : tableDetails.getGameDisplayName());
      gameYear.setText(tableDetails.getGameYear() == null ? "-" : String.valueOf(tableDetails.getGameYear()));
      romName.setText(StringUtils.isEmpty(tableDetails.getRomName()) ? "-" : tableDetails.getRomName());
      gameType.setText(tableDetails.getGameType() == null ? "-" : tableDetails.getGameType());
      gameVersion.setText(StringUtils.isEmpty(tableDetails.getGameVersion()) ? "-" : tableDetails.getGameVersion());
      gameTheme.setText(StringUtils.isEmpty(tableDetails.getGameTheme()) ? "-" : tableDetails.getGameTheme());
      manufacturer.setText(StringUtils.isEmpty(tableDetails.getManufacturer()) ? "-" : tableDetails.getManufacturer());
      numberOfPlayers.setText(tableDetails.getNumberOfPlayers() == null ? "-" : String.valueOf(tableDetails.getNumberOfPlayers()));
      tags.setText(StringUtils.isEmpty(tableDetails.getTags()) ? "-" : tableDetails.getTags());
      category.setText(StringUtils.isEmpty(tableDetails.getCategory()) ? "-" : tableDetails.getCategory());
      author.setText(StringUtils.isEmpty(tableDetails.getAuthor()) ? "-" : tableDetails.getAuthor());
      launchCustomVar.setText(StringUtils.isEmpty(tableDetails.getLaunchCustomVar()) ? "-" : tableDetails.getLaunchCustomVar());
      keepDisplays.setText(StringUtils.isEmpty(tableDetails.getKeepDisplays()) ? "-" : tableDetails.getKeepDisplays());
      gameRating.setText(tableDetails.getGameRating() == null ? "?" : String.valueOf(tableDetails.getGameRating()));
      dof.setText(StringUtils.isEmpty(tableDetails.getDof()) ? "-" : tableDetails.getDof());
      IPDBNum.setText(StringUtils.isEmpty(tableDetails.getIPDBNum()) ? "-" : tableDetails.getIPDBNum());
      altRunMode.setText(StringUtils.isEmpty(tableDetails.getAltRunMode()) ? "-" : tableDetails.getAltRunMode());
      url.setText(StringUtils.isEmpty(tableDetails.getUrl()) ? "-" : tableDetails.getUrl());
      designedBy.setText(StringUtils.isEmpty(tableDetails.getDesignedBy()) ? "-" : tableDetails.getDesignedBy());
      notes.setText(StringUtils.isEmpty(tableDetails.getNotes()) ? "-" : tableDetails.getNotes());
    }
    else {

    }
  }
}
