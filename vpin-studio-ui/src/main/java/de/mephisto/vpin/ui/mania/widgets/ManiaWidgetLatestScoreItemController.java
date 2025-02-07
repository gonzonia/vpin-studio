package de.mephisto.vpin.ui.mania.widgets;

import de.mephisto.vpin.commons.fx.widgets.WidgetController;
import de.mephisto.vpin.connectors.mania.model.TableScoreDetails;
import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.util.ScoreFormatUtil;
import de.mephisto.vpin.restclient.mania.TarcisioWheelsDB;
import de.mephisto.vpin.ui.Studio;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ResourceBundle;

import static de.mephisto.vpin.commons.utils.WidgetFactory.getScoreFont;
import static de.mephisto.vpin.ui.Studio.client;

public class ManiaWidgetLatestScoreItemController extends WidgetController implements Initializable {
  private final static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM-dd-yyyy / hh:mm");

  @FXML
  private BorderPane root;

  @FXML
  private VBox highscoreVBox;

  @FXML
  private ImageView wheelImageView;

  @FXML
  private Label tableLabel;

  @FXML
  private Label nameLabel;

  @FXML
  private Label scoreLabel;

  @FXML
  private Label changeDateLabel;

  @FXML
  private Label installedLabel;

  private ManiaWidgetLatestScoresController latestScoresController;
  private VpsTable vpsTable;
  private TableScoreDetails score;

  @FXML
  private void onScoreClick() {
    latestScoresController.onScoreClick(vpsTable, score);
  }


  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    installedLabel.setVisible(false);
  }

  public void setData(VpsTable vpsTable, TableScoreDetails score) {
    this.vpsTable = vpsTable;
    this.score = score;
    InputStream imageInput = TarcisioWheelsDB.getWheelImage(Studio.class, client, vpsTable.getId());
    Image image = new Image(imageInput);
    wheelImageView.setImage(image);

    tableLabel.setText(vpsTable.getDisplayName());
    nameLabel.setText(score.getDisplayName());

    scoreLabel.setFont(getScoreFont());
    scoreLabel.setText(ScoreFormatUtil.formatScore(score.getScore()));

    String date = simpleDateFormat.format(score.getCreationDate());
    changeDateLabel.setText("Updated: " + date);

    GameRepresentation gameByVpsTable = client.getGameService().getGameByVpsTable(score.getVpsTableId(), null);
    installedLabel.setVisible(gameByVpsTable != null);
  }

  public void setLatestScoresController(ManiaWidgetLatestScoresController latestScoresController) {
    this.latestScoresController = latestScoresController;
  }
}