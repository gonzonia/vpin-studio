package de.mephisto.vpin.restclient.notifications;

import de.mephisto.vpin.restclient.JsonSettings;
import de.mephisto.vpin.restclient.PreferenceNames;

public class NotificationSettings extends JsonSettings {
  private boolean desktopMode = false;

  private boolean startupNotification = false;
  private boolean highscoreUpdatedNotification = true;
  private boolean highscoreCheckedNotification = true;
  private boolean iScoredNotification = true;
  private boolean discordNotification = true;
  private boolean competitionNotification = true;
  private boolean recordingStartNotification = true;
  private boolean recordingEndNotification = true;
  private int notificationsScreenId = -1;

  public int getNotificationsScreenId() {
    return notificationsScreenId;
  }

  public void setNotificationsScreenId(int notificationsScreenId) {
    this.notificationsScreenId = notificationsScreenId;
  }

  private int durationSec = 5;

  public boolean isRecordingStartNotification() {
    return recordingStartNotification;
  }

  public void setRecordingStartNotification(boolean recordingStartNotification) {
    this.recordingStartNotification = recordingStartNotification;
  }

  public boolean isRecordingEndNotification() {
    return recordingEndNotification;
  }

  public void setRecordingEndNotification(boolean recordingEndNotification) {
    this.recordingEndNotification = recordingEndNotification;
  }

  public boolean isDesktopMode() {
    return desktopMode;
  }

  public void setDesktopMode(boolean desktopMode) {
    this.desktopMode = desktopMode;
  }

  public boolean isCompetitionNotification() {
    return competitionNotification;
  }

  public void setCompetitionNotification(boolean competitionNotification) {
    this.competitionNotification = competitionNotification;
  }

  public boolean isDiscordNotification() {
    return discordNotification;
  }

  public void setDiscordNotification(boolean discordNotification) {
    this.discordNotification = discordNotification;
  }

  public boolean isHighscoreCheckedNotification() {
    return highscoreCheckedNotification;
  }

  public void setHighscoreCheckedNotification(boolean highscoreCheckedNotification) {
    this.highscoreCheckedNotification = highscoreCheckedNotification;
  }

  public boolean isiScoredNotification() {
    return iScoredNotification;
  }

  public void setiScoredNotification(boolean iScoredNotification) {
    this.iScoredNotification = iScoredNotification;
  }

  public int getDurationSec() {
    return durationSec;
  }

  public void setDurationSec(int durationSec) {
    this.durationSec = durationSec;
  }

  public boolean isStartupNotification() {
    return startupNotification;
  }

  public void setStartupNotification(boolean startupNotification) {
    this.startupNotification = startupNotification;
  }

  public boolean isHighscoreUpdatedNotification() {
    return highscoreUpdatedNotification;
  }

  public void setHighscoreUpdatedNotification(boolean highscoreUpdatedNotification) {
    this.highscoreUpdatedNotification = highscoreUpdatedNotification;
  }

  @Override
  public String getSettingsName() {
    return PreferenceNames.NOTIFICATION_SETTINGS;
  }
}
