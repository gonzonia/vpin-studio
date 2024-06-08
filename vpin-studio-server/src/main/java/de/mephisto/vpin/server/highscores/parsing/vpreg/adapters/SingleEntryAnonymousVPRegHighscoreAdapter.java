package de.mephisto.vpin.server.highscores.parsing.vpreg.adapters;

import com.thoughtworks.xstream.core.util.Base64Encoder;
import de.mephisto.vpin.server.highscores.parsing.ScoreParsingEntry;
import de.mephisto.vpin.server.highscores.parsing.ScoreParsingSummary;
import org.apache.poi.poifs.filesystem.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public class SingleEntryAnonymousVPRegHighscoreAdapter extends VPRegHighscoreAdapterImpl {

  @Override
  public boolean isApplicable(DirectoryEntry gameFolder) throws IOException {
    if (getHighscoreEntry(gameFolder) != null && !gameFolder.hasEntry("hsa1") && !gameFolder.hasEntry("HSA1")) {
      return true;
    }
    return false;
  }

  @Override
  public ScoreParsingSummary readHighscore(DirectoryEntry gameFolder) throws IOException {
    ScoreParsingSummary summary = new ScoreParsingSummary();

    DocumentEntry scoreEntry = getHighscoreEntry(gameFolder);
    String scoreString = super.getScoreEntry(scoreEntry);
    ScoreParsingEntry score = new ScoreParsingEntry();
    score.setInitials("???");
    score.setScore(parseScoreString(scoreString));
    score.setPos(1);
    summary.getScores().add(score);

    return summary;
  }

  @Override
  public boolean resetHighscore(POIFSFileSystem fs, DirectoryEntry gameFolder) throws IOException {
    DocumentNode highscoreEntry = getHighscoreEntry(gameFolder);
    POIFSDocument scoreDocument = new POIFSDocument(highscoreEntry);
    scoreDocument.replaceContents(new ByteArrayInputStream(new Base64Encoder().decode(VPRegHighscoreAdapter.BASE64_ZERO_SCORE)));
    return true;
  }
}
