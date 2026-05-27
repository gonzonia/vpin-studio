package de.mephisto.vpin.restclient.frontend;

import de.mephisto.vpin.connectors.assets.TableAsset;

import java.util.List;

public class TableAssetSearch {

  private String assetSourceId;
  private int gameId;
  private VPinScreen screen;
  private String term;

  private List<TableAsset> result;

  public String getAssetSourceId() {
    return assetSourceId;
  }

  public void setAssetSourceId(String assetSourceId) {
    this.assetSourceId = assetSourceId;
  }

  public int getGameId() {
    return gameId;
  }

  public void setGameId(int emulatorId) {
    this.gameId = emulatorId;
  }
  
  public VPinScreen getScreen() {
    return screen;
  }

  public void setScreen(VPinScreen screen) {
    this.screen = screen;
  }

  public String getTerm() {
    return term;
  }

  public void setTerm(String term) {
    this.term = term;
  }

  public List<TableAsset> getResult() {
    return result;
  }

  public void setResult(List<TableAsset> result) {
    this.result = result;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof TableAssetSearch that)) return false;

    if (screen != that.screen) return false;
    return term != null ? term.equals(that.term) : that.term == null;
  }

  @Override
  public int hashCode() {
    int result = screen != null ? screen.hashCode() : 0;
    result = 31 * result + (term != null ? term.hashCode() : 0);
    return result;
  }

}
