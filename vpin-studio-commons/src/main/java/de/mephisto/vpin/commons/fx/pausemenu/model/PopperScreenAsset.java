package de.mephisto.vpin.commons.fx.pausemenu.model;

import de.mephisto.vpin.restclient.popper.PinUPPlayerDisplay;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;

import java.io.InputStream;

public class PopperScreenAsset {
  private Stage screenStage;
  private int rotation;
  private PinUPPlayerDisplay display;
  private String mimeType;
  private InputStream inputStream;
  private String url;
  private int duration;
  private String name;

  private MediaPlayer mediaPlayer;

  public void dispose() {
    if(mediaPlayer != null) {
      new Thread(() -> {
        mediaPlayer.dispose();
      }).start();
    }
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public MediaPlayer getMediaPlayer() {
    return mediaPlayer;
  }

  public void setMediaPlayer(MediaPlayer mediaPlayer) {
    this.mediaPlayer = mediaPlayer;
  }

  public int getDuration() {
    return duration;
  }

  public void setDuration(int duration) {
    this.duration = duration;
  }

  public int getRotation() {
    return rotation;
  }

  public void setRotation(int rotation) {
    this.rotation = rotation;
  }

  public Stage getScreenStage() {
    return screenStage;
  }

  public void setScreenStage(Stage screenStage) {
    this.screenStage = screenStage;
  }

  public PinUPPlayerDisplay getDisplay() {
    return display;
  }

  public void setDisplay(PinUPPlayerDisplay display) {
    this.display = display;
  }

  public String getMimeType() {
    return mimeType;
  }

  public void setMimeType(String mimeType) {
    this.mimeType = mimeType;
  }

  public InputStream getInputStream() {
    return inputStream;
  }

  public void setInputStream(InputStream inputStream) {
    this.inputStream = inputStream;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }
}
