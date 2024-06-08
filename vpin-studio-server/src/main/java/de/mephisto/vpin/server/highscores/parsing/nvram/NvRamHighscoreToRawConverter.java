package de.mephisto.vpin.server.highscores.parsing.nvram;

import de.mephisto.vpin.commons.utils.SystemCommandExecutor;
import de.mephisto.vpin.server.highscores.parsing.nvram.adapters.FourColumnScoreAdapter;
import de.mephisto.vpin.server.highscores.parsing.nvram.adapters.ScoreNvRamAdapter;
import de.mephisto.vpin.server.highscores.parsing.nvram.adapters.SinglePlayerScoreAdapter;
import de.mephisto.vpin.server.highscores.parsing.nvram.adapters.SkipFirstListScoreAdapter;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NvRamHighscoreToRawConverter {
  private final static Logger LOG = LoggerFactory.getLogger(NvRamHighscoreToRawConverter.class);

  private final static List<ScoreNvRamAdapter> adapters = new ArrayList<>();

  static {
    adapters.add(new SinglePlayerScoreAdapter("algar_l1.nv", 1));
    adapters.add(new SinglePlayerScoreAdapter("alienstr.nv", 1));
    adapters.add(new SinglePlayerScoreAdapter("alpok_b6.nv", 1));
    adapters.add(new FourColumnScoreAdapter("monopoly.nv"));
    adapters.add(new SkipFirstListScoreAdapter("godzilla.nv"));
    adapters.add(new SinglePlayerScoreAdapter());
  }

  public static String convertNvRamTextToMachineReadable(@NonNull File commandFile, @NonNull File nvRam) throws Exception {
    boolean nvOffset = false;
    File originalNVRamFile = nvRam;
    File backedUpRamFile = nvRam;

    try {
      String nvRamFileName = nvRam.getCanonicalFile().getName().toLowerCase();
      String pinemHiSupportedNVRamName = FilenameUtils.getBaseName(nvRamFileName).toLowerCase();
      if (nvRamFileName.contains(" ")) {
        LOG.info("Stripping NV offset from nvram file \"" + nvRamFileName + "\" to check if supported.");
        pinemHiSupportedNVRamName = nvRamFileName.substring(0, nvRamFileName.indexOf(" "));

        //rename the original nvram file so that we can parse with the original name
        originalNVRamFile = new File(nvRam.getParentFile(), pinemHiSupportedNVRamName + ".nv");
        if (originalNVRamFile.exists()) {
          backedUpRamFile = new File(nvRam.getParentFile(), originalNVRamFile.getName() + ".bak");
          if (backedUpRamFile.exists()) {
            backedUpRamFile.delete();
          }
          FileUtils.copyFile(originalNVRamFile, backedUpRamFile);
          LOG.info("Temporary renamed original nvram file " + originalNVRamFile.getAbsolutePath() + " to " + backedUpRamFile.getAbsolutePath());
          FileUtils.copyFile(nvRam, originalNVRamFile);
          LOG.info("Temporary renamed actual nvram file " + nvRam.getAbsolutePath() + " to " + originalNVRamFile.getAbsolutePath());
        }
        nvOffset = true;
      }

      List<String> commands = Arrays.asList(commandFile.getName(), originalNVRamFile.getName());
      SystemCommandExecutor executor = new SystemCommandExecutor(commands);
      executor.setDir(commandFile.getParentFile());
      executor.executeCommand();
      StringBuilder standardOutputFromCommand = executor.getStandardOutputFromCommand();
      StringBuilder standardErrorFromCommand = executor.getStandardErrorFromCommand();
      if (!StringUtils.isEmpty(standardErrorFromCommand.toString())) {
//        String error = "Pinemhi command (" + commandFile.getCanonicalPath() + " " + pinemHiSupportedNVRamName + ") failed: " + standardErrorFromCommand;
        String error = "Pinemhi command (" + commandFile.getCanonicalPath() + " " + pinemHiSupportedNVRamName + ") failed (details skipped).";
        throw new Exception(error);
      }
      String stdOut = standardOutputFromCommand.toString();
      return convertOutputToRaw(nvRamFileName, stdOut);
    } catch (Exception e) {
      LOG.error(e.getMessage(), e);
      throw e;
    } finally {
      if (nvOffset && originalNVRamFile.delete()) {
        FileUtils.copyFile(backedUpRamFile, originalNVRamFile);
        LOG.info("Restored original nvram " + originalNVRamFile.getAbsolutePath());
      }
    }
  }

  @NotNull
  private static String convertOutputToRaw(@NonNull String nvRamFileName, String stdOut) throws Exception {
    // replace french space character, displayed ÿ with "."
    stdOut = stdOut
      .replaceAll("\u00ff", ".")
      .replaceAll("\ufffd", ".");

    //check for pre-formatting
    List<String> lines = Arrays.asList(stdOut.trim().split("\n"));
    for (ScoreNvRamAdapter adapter : adapters) {
      if (adapter.isApplicable(nvRamFileName, lines)) {
        return adapter.convert(nvRamFileName, lines);
      }
    }
    return stdOut;
  }
}
