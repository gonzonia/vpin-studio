package de.mephisto.vpin.server.highscores.parsing.vpreg.adapters;

import de.mephisto.vpin.server.highscores.parsing.ScoreParsingEntry;
import de.mephisto.vpin.server.highscores.parsing.ScoreParsingSummary;
import org.apache.poi.poifs.filesystem.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class NumericListVPRegHighscoreAdapter extends VPRegHighscoreAdapterImpl {
  private static final String HIGH_SCORE = "HighScore";
  private static final String NAME_SUFFIX = "Name";

  @Override
  public boolean isApplicable(DirectoryEntry gameFolder) {
    if (gameFolder.hasEntry(getScoreKey(1)) && gameFolder.hasEntry(getNameKey(1))) {
      return true;
    }
    return false;
  }

  @Override
  public ScoreParsingSummary readHighscore(DirectoryEntry gameFolder) throws IOException {
    ScoreParsingSummary summary = new ScoreParsingSummary();
    int index = getStartIndex();
    while (gameFolder.hasEntry(getScoreKey(index)) && gameFolder.hasEntry(getNameKey(index))) {
      DocumentEntry nameEntry = (DocumentEntry) gameFolder.getEntry(getNameKey(index));
      String nameString = super.getNameString(nameEntry);

      DocumentEntry scoreEntry = (DocumentEntry) gameFolder.getEntry(getScoreKey(index));
      String scoreString = super.getScoreEntry(scoreEntry);

      ScoreParsingEntry score = new ScoreParsingEntry();
      score.setInitials(nameString);
      score.setScore(parseScoreString(scoreString));
      score.setPos(index);

      if (getStartIndex() == 0) {
        score.setPos(score.getPos() + 1);
      }
      summary.getScores().add(score);
      index++;
    }
    return summary;
  }

  @Override
  public boolean resetHighscore(POIFSFileSystem fs, DirectoryEntry gameFolder, long score) throws IOException {
    int index = getStartIndex();
    while (gameFolder.hasEntry(getScoreKey(index)) && gameFolder.hasEntry(getNameKey(index))) {
      String scoreKey = getHighScorePrefix() + index;
      DocumentNode scoreEntry = (DocumentNode) gameFolder.getEntry(scoreKey);
      POIFSDocument scoreDocument = new POIFSDocument(scoreEntry);
      byte[] array = StandardCharsets.UTF_16LE.encode(String.valueOf(score)).array();
      scoreDocument.replaceContents(new ByteArrayInputStream(array));

      String nameKey = getNameKey(index);
      DocumentNode nameEntry = (DocumentNode) gameFolder.getEntry(nameKey);
      POIFSDocument nameDocument = new POIFSDocument(nameEntry);
      array = StandardCharsets.UTF_16LE.encode("???").array();
      nameDocument.replaceContents(new ByteArrayInputStream(array));

      index++;

      fs.writeFilesystem();
    }
    return true;
  }

  protected int getStartIndex() {
    return 1;
  }

  protected String getNameKey(int index) {
    return getHighScorePrefix() + index + getNamePrefix();
  }

  protected String getScoreKey(int index) {
    return getHighScorePrefix() + index;
  }

  protected String getHighScorePrefix() {
    return HIGH_SCORE;
  }

  protected String getNamePrefix() {
    return NAME_SUFFIX;
  }
}
