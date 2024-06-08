package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.commons.fx.ConfirmationResult;
import de.mephisto.vpin.commons.fx.widgets.WidgetController;
import de.mephisto.vpin.commons.utils.ScoreGraphUtil;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.highscores.*;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.util.MediaUtil;
import de.mephisto.vpin.ui.util.ProgressDialog;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import eu.hansolo.tilesfx.Tile;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.SplitMenuButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

import static de.mephisto.vpin.ui.Studio.client;

public class TablesSidebarHighscoresController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(TablesSidebarHighscoresController.class);

  @FXML
  private Label hsTypeLabel;

  @FXML
  private Label hsFileLabel;

  @FXML
  private Label hsLastModifiedLabel;

  @FXML
  private Label hsLastScannedLabel;

  @FXML
  private Label hsRecordLabel;

  @FXML
  private VBox formattedScoreWrapper;

  @FXML
  private VBox rawScoreWrapper;

  @FXML
  private VBox scoreGraphWrapper;

  @FXML
  private Button resetBtn;

  @FXML
  private SplitMenuButton scanHighscoreBtn;

  @FXML
  private Button cardBtn;

  @FXML
  private Button backupBtn;

  @FXML
  private Button restoreBtn;

  @FXML
  private Label rawScoreLabel;

  @FXML
  private Label formattedScoreLabel;

  @FXML
  private Label rawTitleLabel;

  @FXML
  private Label formattedTitleLabel;

  @FXML
  private Label backupCountLabel;

  @FXML
  private BorderPane scoreGraph;

  @FXML
  private Label statusLabel;

  @FXML
  private VBox statusPane;

  @FXML
  private VBox dataPane;

  @FXML
  private Button vpSaveEditBtn;


  private Optional<GameRepresentation> game = Optional.empty();

  private TablesSidebarController tablesSidebarController;
  private List<HighscoreBackup> highscoreBackups;

  // Add a public no-args constructor
  public TablesSidebarHighscoresController() {
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    statusPane.managedProperty().bindBidirectional(statusPane.visibleProperty());
    vpSaveEditBtn.setVisible(client.getSystemService().isLocal());
  }

  @FXML
  public void onVPSaveEdit() {
    try {
      ProcessBuilder builder = new ProcessBuilder(new File("resources", "VPSaveEdit.exe").getAbsolutePath());
      builder.directory(new File("resources"));
      builder.start();
    } catch (IOException e) {
      LOG.error("Failed to open VPSaveEdit: " + e.getMessage(), e);
      WidgetFactory.showAlert(Studio.stage, "Error", "Failed to open VPSaveEdit: " + e.getMessage());
    }
  }

  @FXML
  private void onCard() {
    if (this.game.isPresent()) {
      GameRepresentation g = this.game.get();
      boolean b = Studio.client.getHighscoreCardsService().generateHighscoreCardSample(g);
      if (b) {
        ByteArrayInputStream s = Studio.client.getHighscoreCardsService().getHighscoreCard(g);
        MediaUtil.openMedia(s);
      }
      else {
        WidgetFactory.showAlert(Studio.stage, "Card Generation Failed.", "The card generation failed, check log files for details.");
      }
    }
  }

  @FXML
  private void onScan() {
    this.refreshView(game, true);
  }

  @FXML
  private void onScanAll() {
    Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Scan for highscores updates of all " + client.getGameService().getVpxGamesCached().size() + " tables?");
    if (result.isPresent() && result.get().equals(ButtonType.OK)) {
      ProgressDialog.createProgressDialog(new TableHighscoresScanProgressModel(client.getGameService().getVpxGamesCached()));
      EventManager.getInstance().notifyTablesChanged();
    }
  }

  @FXML
  private void onBackup() {
    if (this.game.isPresent()) {
      GameRepresentation g = this.game.get();
      String last = null;
      if (highscoreBackups != null && !this.highscoreBackups.isEmpty()) {
        last = "The last backup was created at " + this.highscoreBackups.get(0);
      }

      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Create highscore backup for table \"" + g.getGameDisplayName() + "\"?", last);
      if (result.isPresent() && result.get().equals(ButtonType.OK)) {
        try {
          Studio.client.getHigscoreBackupService().backup(g.getId());
        } catch (Exception e) {
          LOG.error("Failed to back highscore: " + e.getMessage(), e);
          WidgetFactory.showAlert(Studio.stage, "Error", "Failed create highscore backup: " + e.getMessage());
        }
        EventManager.getInstance().notifyTableChange(g.getId(), g.getRom());
      }
    }
  }

  @FXML
  private void onRestore() {
    if (this.game.isPresent()) {
      GameRepresentation g = this.game.get();
      if (StringUtils.isEmpty(g.getRom()) && StringUtils.isEmpty(g.getTableName())) {
        WidgetFactory.showAlert(Studio.stage, "ROM name is missing.",
          "To backup the the highscore of a table, the ROM name or tablename must have been resolved.",
          "You can enter the values for this manually in the \"Script Details\" section.");
      }
      else {
        TableDialogs.openHighscoresAdminDialog(tablesSidebarController, this.game.get());
      }
    }
  }

  @FXML
  private void onScoreReset() {
    if (this.game.isPresent()) {
      GameRepresentation g = this.game.get();
      ConfirmationResult confirmationResult = WidgetFactory.showAlertOptionWithMandatoryCheckbox(Studio.stage, "Reset Highscores", "Cancel", "Reset Highscores", "Reset the highscores of \"" + g.getGameDisplayName() + "\"?",
        "An automatic backup will be made before the scores are deleted.", "Yes, I know what I'm doing.");
      if (confirmationResult.isChecked() && !confirmationResult.isApplyClicked()) {
        if (!Studio.client.getGameService().resetHighscore(g.getId())) {
          WidgetFactory.showAlert(Studio.stage, "Error", "Reset Failed", "Check the log files for details and make sure that no process is blocking the highscore file.");
        }
        this.refreshView(this.game, true);
      }
    }
  }

  public void setGame(Optional<GameRepresentation> game) {
    this.highscoreBackups = new ArrayList<>();
    this.game = game;
    this.refreshView(game, false);
  }

  public void refreshView(Optional<GameRepresentation> g, boolean forceRescan) {
    rawScoreLabel.setText("");
    formattedScoreLabel.setText("");

    this.hsFileLabel.setText("-");
    this.hsTypeLabel.setText("-");
    this.hsLastModifiedLabel.setText("-");
    this.hsLastScannedLabel.setText("-");
    this.hsRecordLabel.setText("-");
    this.backupCountLabel.setText("-");

    rawTitleLabel.setVisible(false);
    formattedTitleLabel.setVisible(false);

    rawScoreWrapper.setVisible(false);
    formattedScoreWrapper.setVisible(false);
    scoreGraphWrapper.setVisible(false);

    scanHighscoreBtn.setDisable(true);
    cardBtn.setDisable(true);
    resetBtn.setDisable(true);

    backupBtn.setDisable(true);
    restoreBtn.setText("Restore");

    if (g.isPresent()) {
      GameRepresentation game = g.get();
      scanHighscoreBtn.setDisable(false);
      restoreBtn.setDisable(false);

      String rom = game.getRom();
      if (StringUtils.isEmpty(rom)) {
        rom = game.getTableName();
      }

      if (StringUtils.isEmpty(rom)) {
        backupCountLabel.setText("0");
      }
      else {
        highscoreBackups = Studio.client.getHigscoreBackupService().get(rom);
        backupCountLabel.setText(String.valueOf(highscoreBackups.size()));
        if (!highscoreBackups.isEmpty()) {
          restoreBtn.setText("Restore (" + highscoreBackups.size() + ")");
        }
      }


      ProgressResultModel progressDialog = ProgressDialog.createProgressDialog(new TableHighscoresScanProgressModel(Arrays.asList(game)));
      HighscoreMetadataRepresentation metadata = null;
      if (!progressDialog.getResults().isEmpty()) {
        metadata = (HighscoreMetadataRepresentation) progressDialog.getResults().get(0);
      }

      boolean hasHighscore = metadata != null && metadata.getStatus() == null && !StringUtils.isEmpty(metadata.getRaw());
      dataPane.setVisible(hasHighscore);
      statusPane.setVisible(!hasHighscore);

      statusLabel.setText("Unknown status.");
      if (metadata != null) {
        if (!hasHighscore) {
          if (!StringUtils.isEmpty(metadata.getStatus())) {
            statusLabel.setText(metadata.getStatus());
          }
          else {
            statusLabel.setText("Unknown status.");
          }
        }

        backupBtn.setDisable(metadata.getType() == null);
        restoreBtn.setDisable(metadata.getType() == null && (highscoreBackups == null || highscoreBackups.isEmpty()));

        if (metadata.getFilename() != null) {
          this.hsFileLabel.setText(metadata.getFilename());
        }

        if (metadata.getType() != null) {
          this.hsTypeLabel.setText(metadata.getType());
        }

        if (metadata.getModified() != null) {
          this.hsLastModifiedLabel.setText(SimpleDateFormat.getDateTimeInstance().format(metadata.getModified()));
        }

        if (metadata.getScanned() != null) {
          this.hsLastScannedLabel.setText(SimpleDateFormat.getDateTimeInstance().format(metadata.getScanned()));
        }

        if (!StringUtils.isEmpty(metadata.getRaw())) {
          rawTitleLabel.setVisible(true);
          rawScoreWrapper.setVisible(true);
          rawScoreLabel.setFont(WidgetController.getScoreFontText());
          rawScoreLabel.setText(metadata.getRaw());
        }

        ScoreSummaryRepresentation summary = Studio.client.getGameService().getGameScores(game.getId());
        if (!summary.getScores().isEmpty()) {
          cardBtn.setDisable(false);
          resetBtn.setDisable(StringUtils.isEmpty(rom));

          scoreGraphWrapper.setVisible(true);

          List<ScoreRepresentation> scores = summary.getScores();
          StringBuilder builder = new StringBuilder();
          for (ScoreRepresentation score : scores) {
            builder.append("#");
            builder.append(score.getPosition());
            builder.append(" ");
            builder.append(score.getPlayerInitials());
            builder.append("   ");
            builder.append(score.getFormattedScore());
            builder.append("\n");
          }

          formattedTitleLabel.setVisible(true);
          formattedScoreWrapper.setVisible(true);

          formattedScoreLabel.setFont(WidgetController.getScoreFontText());
          formattedScoreLabel.setText(builder.toString());
        }
      }

      ScoreListRepresentation scoreHistory = Studio.client.getGameService().getScoreHistory(game.getId());
      hsRecordLabel.setText(String.valueOf(scoreHistory.getScores().size()));
      if (!scoreHistory.getScores().isEmpty()) {
        Tile highscoresGraphTile = ScoreGraphUtil.createGraph(scoreHistory);
        try {
          scoreGraph.setCenter(highscoresGraphTile);
        } catch (Exception e) {
          //ignore
        }
      }
    }
  }

  public void setSidebarController(TablesSidebarController tablesSidebarController) {
    this.tablesSidebarController = tablesSidebarController;
  }
}