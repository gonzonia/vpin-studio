package de.mephisto.vpin.server.exporter;

import de.mephisto.vpin.restclient.altcolor.AltColorTypes;
import de.mephisto.vpin.restclient.highscores.HighscoreType;
import de.mephisto.vpin.restclient.util.DateUtil;
import de.mephisto.vpin.restclient.validation.ValidationState;
import de.mephisto.vpin.server.games.GameEmulator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.time.OffsetDateTime;

public class ExportEntityConverter {
  private final static Logger LOG = LoggerFactory.getLogger(ExportEntityConverter.class);

  public static String convert(String name, Object property) {
      switch (property) {
          case String string -> {
          }
          case Boolean b -> {
          }
          case Integer i -> {
          }
          case Long l -> {
          }
          case File f -> property = f.exists();
          case ValidationState s -> property = s.getCode();
          case OffsetDateTime s -> property = DateUtil.formatDateTime(s);
          case HighscoreType s -> property = s.name();
          case AltColorTypes s -> property = s.name();
          case GameEmulator s -> property = s.getName();
          default -> LOG.warn("Unmapped field type: {}, field name:{}", property.getClass().getSimpleName(), name);
      }
    return String.valueOf(property)
        .replaceAll("\n", " ")
        .replaceAll("\r", "")
        .replaceAll("\t", "");
  }
}
