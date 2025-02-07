package de.mephisto.vpin.restclient.cards;

import de.mephisto.vpin.restclient.JsonSettings;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.highscores.HighscoreCardResolution;

/**
 *
 */
public class CardSettings extends JsonSettings {
  private String popperScreen = null;
  private int notificationTime = 0;
  private String notificationRotation = "0";
  private boolean notificationOnPopperScreen = false;
  private HighscoreCardResolution cardResolution = HighscoreCardResolution.HDReady;

  public HighscoreCardResolution getCardResolution() {
    return cardResolution;
  }

  public void setCardResolution(HighscoreCardResolution cardResolution) {
    this.cardResolution = cardResolution;
  }

  public String getPopperScreen() {
    return popperScreen;
  }

  public void setPopperScreen(String popperScreen) {
    this.popperScreen = popperScreen;
  }

  public int getNotificationTime() {
    return notificationTime;
  }

  public void setNotificationTime(int notificationTime) {
    this.notificationTime = notificationTime;
  }

  public String getNotificationRotation() {
    return notificationRotation;
  }

  public void setNotificationRotation(String notificationRotation) {
    this.notificationRotation = notificationRotation;
  }

  public boolean isNotificationOnPopperScreen() {
    return notificationOnPopperScreen;
  }

  public void setNotificationOnPopperScreen(boolean notificationOnPopperScreen) {
    this.notificationOnPopperScreen = notificationOnPopperScreen;
  }

  @Override
  public String getSettingsName() {
    return PreferenceNames.HIGHSCORE_CARD_SETTINGS;
  }
}
