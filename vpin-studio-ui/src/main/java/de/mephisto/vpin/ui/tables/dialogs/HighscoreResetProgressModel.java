package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;

import static de.mephisto.vpin.commons.fx.pausemenu.UIDefaults.MAX_REFRESH_COUNT;
import static de.mephisto.vpin.ui.Studio.client;

public class HighscoreResetProgressModel extends ProgressModel<GameRepresentation> {
  private final static Logger LOG = LoggerFactory.getLogger(HighscoreResetProgressModel.class);
  private List<GameRepresentation> games;

  private final Iterator<GameRepresentation> gameIterator;
  private final long value;

  public HighscoreResetProgressModel(List<GameRepresentation> games, long value) {
    super("Resetting Highscores");
    this.games = games;
    this.gameIterator = games.iterator();
    this.value = value;
  }

  @Override
  public boolean isShowSummary() {
    return false;
  }

  @Override
  public boolean isIndeterminate() {
    return games.size() == 1;
  }

  @Override
  public int getMax() {
    return games.size();
  }

  @Override
  public boolean hasNext() {
    return this.gameIterator.hasNext();
  }

  @Override
  public GameRepresentation getNext() {
    return gameIterator.next();
  }

  @Override
  public String nextToString(GameRepresentation game) {
    return game.getGameDisplayName();
  }

  @Override
  public void processNext(ProgressResultModel progressResultModel, GameRepresentation game) {
    try {
      client.getGameService().resetHighscore(game.getId(), value);
      if (games.size() <= MAX_REFRESH_COUNT) {
        EventManager.getInstance().notifyTableChange(game.getId(), null);
      }
    }
    catch (Exception e) {
      LOG.error("Failed to reset highscore: " + e.getMessage(), e);
      Platform.runLater(() -> {
        WidgetFactory.showAlert(Studio.stage, "Error", "Failed to reset highscore: " + e.getMessage());
      });
    }
  }
}
