package de.mephisto.vpin.ui.competitions.validation;

import de.mephisto.vpin.restclient.competitions.CompetitionType;
import de.mephisto.vpin.restclient.competitions.CompetitionRepresentation;
import de.mephisto.vpin.ui.util.LocalizedValidation;
import edu.umd.cs.findbugs.annotations.NonNull;

import static de.mephisto.vpin.restclient.competitions.CompetitionValidationCode.*;

public class CompetitionValidationTexts {

  @NonNull
  public static LocalizedValidation getValidationResult(@NonNull CompetitionRepresentation competition) {
    String text;
    String label;
    int code = competition.getValidationState().getCode();
    String typeName = "competition";
    if(competition.getType().equals(CompetitionType.SUBSCRIPTION.name())) {
      typeName = "subscription";
    }
    if(competition.getType().equals(CompetitionType.ISCORED.name())) {
      typeName = "iScored subscription";
    }

    switch (code) {
      case DISCORD_SERVER_NOT_FOUND: {
        label = "Invalid Discord server.";
        text = "The Discord server configured for this " + typeName + " was not found.";
        break;
      }
      case DISCORD_CHANNEL_NOT_FOUND: {
        label = "Invalid Discord channel.";
        text = "The Discord channel configured for this " + typeName + " was not found.";
        break;
      }
      case GAME_NOT_FOUND: {
        label = "No matching table found.";
        text = "No matching table was found for this " + typeName + ".";
        break;
      }
      default: {
        throw new UnsupportedOperationException("unmapped competition validation state");
      }

    }

    return new LocalizedValidation(label, text);
  }
}
