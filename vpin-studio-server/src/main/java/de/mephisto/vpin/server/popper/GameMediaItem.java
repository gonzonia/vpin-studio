package de.mephisto.vpin.server.popper;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.mephisto.vpin.restclient.popper.PopperScreen;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.util.MimeTypeUtil;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class GameMediaItem {
  private final static Logger LOG = LoggerFactory.getLogger(GameMediaItem.class);

  private String mimeType;
  private String uri;
  private File file;
  private PopperScreen screen;
  private int gameId;

  public GameMediaItem(@NonNull Game game, @NonNull PopperScreen screen, @NonNull File file) {
    this.file = file;
    this.gameId = game.getId();
    this.screen = screen;
    this.uri = "poppermedia/" + game.getId() + "/" + screen.name();
    this.mimeType = MimeTypeUtil.determineMimeType(file);
  }

  @JsonIgnore
  @NonNull
  public File getFile() {
    return file;
  }

  public PopperScreen getScreen() {
    return screen;
  }

  public String getMimeType() {
    return mimeType;
  }

  public void setMimeType(String mimeType) {
    this.mimeType = mimeType;
  }

  public String getName() {
    return this.file.getName();
  }

  public int getGameId() {
    return gameId;
  }

  public void setGameId(int gameId) {
    this.gameId = gameId;
  }

  public String getUri() {
    return uri;
  }

  public void setUri(String uri) {
    this.uri = uri;
  }
}
