package de.mephisto.vpin.server.highscores.parsing.text.adapters;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TwoPlayersAdapter implements ScoreTextFileAdapter {
  private String name;
  private int scoreLine1 = 1;
  private int scoreLine2 = 2;

  private int lineCount;

  public TwoPlayersAdapter(int lineCount) {
    this.lineCount = lineCount;
  }

  public TwoPlayersAdapter(String name, int scoreLine1, int scoreLine2) {
    this.name = name;
    this.scoreLine1 = scoreLine1;
    this.scoreLine2 = scoreLine2;
  }

  @Override
  public boolean isApplicable(@NotNull File file, @NotNull List<String> lines) {
    if (file.getName().equals(name)) {
      return true;
    }
    return lines.size() == lineCount;
  }

  @Override
  public List<String> resetHighscore(@NotNull File file, @NotNull List<String> lines) {
    List<String> newScoreText = new ArrayList<>();

    for (int i = 0; i < lines.size(); i++) {
      String line = lines.get(i);
      if (i == scoreLine1 || i == scoreLine2) {
        newScoreText.add("0");
        continue;
      }
      newScoreText.add(line);
    }
    return newScoreText;
  }

  @Override
  public String convert(@NotNull File file, @NotNull List<String> lines) {
    StringBuilder builder = new StringBuilder("HIGHEST SCORES\n");
    String score1 = lines.get(scoreLine1);
    String score2 = lines.get(scoreLine2);
    builder.append("#1");
    builder.append(" ");
    builder.append("???");
    builder.append("   ");
    builder.append(score2);
    builder.append("\n");

    builder.append("#2");
    builder.append(" ");
    builder.append("???");
    builder.append("   ");
    builder.append(score1);
    builder.append("\n");

    return builder.toString();
  }
}
