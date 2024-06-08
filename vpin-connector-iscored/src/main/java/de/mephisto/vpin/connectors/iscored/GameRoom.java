package de.mephisto.vpin.connectors.iscored;

import java.util.ArrayList;
import java.util.List;

public class GameRoom {

  private int roomID;
  private String name;
  private String url;

  private List<IScoredGame> games = new ArrayList<>();

  private Settings settings;

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public Settings getSettings() {
    return settings;
  }

  public void setSettings(Settings settings) {
    this.settings = settings;
  }

  public int getRoomID() {
    return roomID;
  }

  public void setRoomID(int roomID) {
    this.roomID = roomID;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<IScoredGame> getGames() {
    return games;
  }

  public IScoredGame getGameByVps(String vpsTableId, String vpsVersionId) {
    for (IScoredGame game : this.games) {
      List<String> tags = game.getTags();
      for (String tag : tags) {
        if(tag.contains(vpsTableId) && tag.contains(vpsVersionId)) {
          return game;
        }
      }
    }
    return null;
  }

  public void setGames(List<IScoredGame> games) {
    this.games = games;
  }
}
