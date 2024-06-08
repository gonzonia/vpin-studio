package de.mephisto.vpin.ui.players;

import de.mephisto.vpin.restclient.players.PlayerRepresentation;
import de.mephisto.vpin.ui.NavigationController;
import de.mephisto.vpin.ui.StudioFXController;
import de.mephisto.vpin.ui.players.dialogs.PlayerScoreLoadingProgressModel;
import de.mephisto.vpin.ui.util.ProgressDialog;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Optional;
import java.util.ResourceBundle;

public class PlayersController implements Initializable, StudioFXController {
  private final static Logger LOG = LoggerFactory.getLogger(PlayersController.class);

  @FXML
  private Tab discordTab;

  @FXML
  private Tab buildInUsersTab;

  @FXML
  private TabPane tabPane;

  @FXML
  private Label noScoreLabel;

  @FXML
  private VBox highscoreList;

  @FXML
  private Node validationError;

  @FXML
  private Label errorTextLabel;

  @FXML
  private Label errorTitleLabel;

  @FXML
  private Label playerCountLabel;

  @FXML
  private TitledPane highscoresTitledPane;

  private BuiltInPlayersController builtInPlayersController;
  private DiscordPlayersController discordPlayersController;

  // Add a public no-args constructor
  public PlayersController() {
  }

  public void updateSelection(Optional<PlayerRepresentation> player) {
    updateForTabSelection(player);
    validationError.setVisible(false);

    highscoreList.getChildren().removeAll(highscoreList.getChildren());
    noScoreLabel.setVisible(false);
    highscoresTitledPane.setText("Player Highscores");
    if (player.isPresent()) {
      PlayerRepresentation p = player.get();
      if (StringUtils.isEmpty(p.getInitials())) {
        noScoreLabel.setVisible(true);
        noScoreLabel.setText("Player has no initials, no highscores could be resolved.");
        return;
      }

      if (!StringUtils.isEmpty(p.getDuplicatePlayerName())) {
        validationError.setVisible(true);
        errorTextLabel.setText("Player '" + p.getName() + "' has the same initials like user \"" + p.getDuplicatePlayerName() + "\". Change the initials for one of them.");
      }

      highscoresTitledPane.setText("Player Highscores \"" + player.get().getName() + "\"");

      ProgressDialog.createProgressDialog(new PlayerScoreLoadingProgressModel(p, highscoreList, noScoreLabel));
    }
    else {
      noScoreLabel.setText("");
    }
  }

  private void updateForTabSelection(Optional<PlayerRepresentation> player) {
    int index = tabPane.getSelectionModel().selectedIndexProperty().get();
    if (index == 0) {
      if (player.isPresent()) {
        NavigationController.setBreadCrumb(Arrays.asList("Players", "Build-In Players", player.get().getName()));
      }
      else {
        NavigationController.setBreadCrumb(Arrays.asList("Players", "Build-In Players"));
      }

      playerCountLabel.setText(builtInPlayersController.getCount() + " players");
    }
    else {
      if (player.isPresent()) {
        NavigationController.setBreadCrumb(Arrays.asList("Players", "Discord Players", player.get().getName()));
      }
      else {
        NavigationController.setBreadCrumb(Arrays.asList("Players", "Discord Players"));
      }

      playerCountLabel.setText(discordPlayersController.getCount() + " players");
    }
  }


  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    NavigationController.setBreadCrumb(Arrays.asList("Players", "Build-In Players"));
    validationError.setVisible(false);
    noScoreLabel.managedProperty().bindBidirectional(noScoreLabel.visibleProperty());

    try {
      FXMLLoader loader = new FXMLLoader(BuiltInPlayersController.class.getResource("tab-builtin-users.fxml"));
      Parent builtInRoot = loader.load();
      builtInPlayersController = loader.getController();
      builtInPlayersController.setPlayersController(this);
      buildInUsersTab.setContent(builtInRoot);
    } catch (IOException e) {
      LOG.error("failed to load buildIn players: " + e.getMessage(), e);
    }

    try {
      FXMLLoader loader = new FXMLLoader(DiscordPlayersController.class.getResource("tab-discord-users.fxml"));
      Parent builtInRoot = loader.load();
      discordPlayersController = loader.getController();
      discordPlayersController.setPlayersController(this);
      discordTab.setContent(builtInRoot);
    } catch (IOException e) {
      LOG.error("failed to load buildIn players: " + e.getMessage(), e);
    }

    tabPane.getSelectionModel().selectedIndexProperty().addListener((observableValue, number, t1) -> {
      refreshTabSelection(t1);
    });

    updateForTabSelection(Optional.empty());
  }

  private void refreshTabSelection(Number t1) {
    if (t1.intValue() == 0) {
      NavigationController.setBreadCrumb(Arrays.asList("Players", "Build-In Players"));
      Optional<PlayerRepresentation> selection = builtInPlayersController.getSelection();
      updateSelection(selection);
    }
    else {
      NavigationController.setBreadCrumb(Arrays.asList("Players", "Discord Players"));
      Optional<PlayerRepresentation> selection = discordPlayersController.getSelection();
      updateSelection(selection);
    }
  }

  @Override
  public void onViewActivated() {
    refreshTabSelection(tabPane.getSelectionModel().getSelectedIndex());
  }
}