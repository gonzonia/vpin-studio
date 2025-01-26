package de.mephisto.vpin.restclient.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ScoreFormatUtil {
  private final static Logger LOG = LoggerFactory.getLogger(ScoreFormatUtil.class);

  private static Map<Locale, DecimalFormat> formats = new HashMap<>();

  public static String formatScore(long score) {
    return formatScore(score, Locale.getDefault());
  }

  public static String formatScore(long score, Locale loc) {
    try {
      //score = cleanScore(score);

      DecimalFormat decimalFormat = formats.get(loc);
      if (decimalFormat == null) {
        DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(loc);
        decimalFormat = new DecimalFormat("#.##", symbols);
        decimalFormat.setGroupingUsed(true);
        decimalFormat.setGroupingSize(3);
        formats.put(loc, decimalFormat);
      }

      return decimalFormat.format(score);
    }
    catch (NumberFormatException e) {
      LOG.error("Failed to read number from '" + score + "': " + e.getMessage());
      return "0";
    }
  }

  public static String cleanScore(String score) {
    return score.replace(".", "")
        .replace(",", "")
        .replace("?", "")
        .replace("\u00ff", "")
        .replace("\u00a0", "")
        .replace("\u202f", "")
        .replace("\ufffd", "")
        .replace(" ", "");
  }

}
