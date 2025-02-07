package de.mephisto.vpin.server.highscores;

import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.listeners.EventOrigin;
import edu.umd.cs.findbugs.annotations.NonNull;

public class HighscoreChangeEvent {

  @NonNull
  private final Game game;

  private final int scoreCount;
  @NonNull
  private final Score oldScore;
  @NonNull
  private final Score newScore;

  private final boolean initialScore;
  private final EventOrigin eventOrigin;

  private String newRaw;

  private boolean eventReplay;

  public HighscoreChangeEvent(@NonNull Game game, @NonNull Score oldScore, @NonNull Score newScore, @NonNull String newRaw, int scoreCount, boolean initialScore, EventOrigin eventOrigin) {
    this.game = game;
    this.scoreCount = scoreCount;
    this.oldScore = oldScore;
    this.newScore = newScore;
    this.newRaw = newRaw;
    this.initialScore = initialScore;
    this.eventOrigin = eventOrigin;
  }

  public EventOrigin getEventOrigin() {
    return eventOrigin;
  }

  public String getNewRaw() {
    return newRaw;
  }

  public boolean isEventReplay() {
    return eventReplay;
  }

  public void setEventReplay(boolean eventReplay) {
    this.eventReplay = eventReplay;
  }

  public boolean isInitialScore() {
    return initialScore;
  }

  public int getScoreCount() {
    return scoreCount;
  }

  @NonNull
  public Game getGame() {
    return game;
  }

  @NonNull
  public Score getOldScore() {
    return oldScore;
  }

  @NonNull
  public Score getNewScore() {
    return newScore;
  }
}
