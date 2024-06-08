package de.mephisto.vpin.server.mame;

import de.mephisto.vpin.commons.utils.ZipUtil;
import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.games.descriptors.UploadDescriptor;
import de.mephisto.vpin.restclient.mame.MameOptions;
import de.mephisto.vpin.restclient.util.UploaderAnalysis;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameEmulator;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.popper.PopperService;
import de.mephisto.vpin.server.util.WinRegistry;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * See https://www.vpforums.org/index.php?showtopic=37182
 * for a description about all mame options.
 */
@Service
public class MameService implements InitializingBean, ApplicationContextAware {
  private final static Logger LOG = LoggerFactory.getLogger(MameService.class);

  private final static String KEY_SKIP_STARTUP_TEST = "cheat";
  private final static String KEY_USE_SOUND = "sound";
  private final static String KEY_USE_SAMPLES = "samples";
  private final static String KEY_DMD_COMPACT = "dmd_compact";
  private final static String KEY_DMD_DOUBLE_SIZE = "dmd_doublesize";
  private final static String KEY_IGNORE_ROM_ERRORS = "ignore_rom_crc";
  private final static String KEY_CABINET_MODE = "cabinet_mode";
  private final static String KEY_SHOW_DMD = "showwindmd";
  private final static String KEY_USER_EXTERNAL_DMD = "showpindmd";
  private final static String KEY_COLORIZE_DMD = "dmd_colorize";
  private final static String KEY_SOUND_MODE = "sound_mode";
  private final static String KEY_FORCE_STEREO = "force_stereo";

  public final static String MAME_REG_FOLDER_KEY = "SOFTWARE\\Freeware\\Visual PinMame\\";

  private final Map<String, MameOptions> mameCache = new ConcurrentHashMap<>();

  @Autowired
  private PopperService popperService;

  private ApplicationContext applicationContext;

  public boolean clearCache() {
    long l = System.currentTimeMillis();
    mameCache.clear();
    List<String> romFolders = WinRegistry.getCurrentUserKeys(MAME_REG_FOLDER_KEY);
    LOG.info("Reading of " + romFolders.size() + " total mame options (" + (System.currentTimeMillis() - l) + "ms)");

    GameService gameService = applicationContext.getBean(GameService.class);
    List<Game> knownGames = gameService.getKnownGames(-1);
    for (String romFolder : romFolders) {
      Optional<Game> first = knownGames.stream().filter(g -> (g.getRom() != null && g.getRom().equalsIgnoreCase(romFolder)) || (g.getRomAlias() != null && g.getRomAlias().equalsIgnoreCase(romFolder))).findFirst();
      if (first.isPresent()) {
        mameCache.put(romFolder.toLowerCase(), getOptions(romFolder));
      }
    }
    LOG.info("Read " + this.mameCache.size() + " mame options (" + (System.currentTimeMillis() - l) + "ms)");
    return true;
  }

  @NonNull
  public MameOptions getOptions(@NonNull String rom) {
    if (mameCache.containsKey(rom.toLowerCase())) {
      return mameCache.get(rom.toLowerCase());
    }

    List<String> romFolders = WinRegistry.getCurrentUserKeys(MAME_REG_FOLDER_KEY);
    MameOptions options = new MameOptions();
    options.setRom(rom);
    options.setExistInRegistry(romFolders.contains(rom.toLowerCase()));

    if (options.isExistInRegistry()) {
      Map<String, Object> values = WinRegistry.getCurrentUserValues(MAME_REG_FOLDER_KEY + rom);

      options.setSkipPinballStartupTest(getBoolean(values, KEY_SKIP_STARTUP_TEST));
      options.setUseSound(getBoolean(values, KEY_USE_SOUND));
      options.setUseSamples(getBoolean(values, KEY_USE_SAMPLES));
      options.setCompactDisplay(getBoolean(values, KEY_DMD_COMPACT));
      options.setDoubleDisplaySize(getBoolean(values, KEY_DMD_DOUBLE_SIZE));
      options.setUseSamples(getBoolean(values, KEY_USE_SAMPLES));
      options.setIgnoreRomCrcError(getBoolean(values, KEY_IGNORE_ROM_ERRORS));
      options.setCabinetMode(getBoolean(values, KEY_CABINET_MODE));
      options.setShowDmd(getBoolean(values, KEY_SHOW_DMD));
      options.setUseExternalDmd(getBoolean(values, KEY_USER_EXTERNAL_DMD));
      options.setColorizeDmd(getBoolean(values, KEY_COLORIZE_DMD));
      options.setSoundMode(getBoolean(values, KEY_SOUND_MODE));
      options.setForceStereo(getBoolean(values, KEY_FORCE_STEREO));
    }

    mameCache.put(options.getRom().toLowerCase(), options);
    return options;
  }

  public MameOptions saveOptions(@NonNull MameOptions options) {
    String rom = options.getRom();
    options.setExistInRegistry(true);

    if (!options.isExistInRegistry()) {
      WinRegistry.createKey(MAME_REG_FOLDER_KEY + rom);
    }
    WinRegistry.setIntValue(MAME_REG_FOLDER_KEY + rom, KEY_SKIP_STARTUP_TEST, options.isSkipPinballStartupTest() ? 1 : 0);
    WinRegistry.setIntValue(MAME_REG_FOLDER_KEY + rom, KEY_USE_SOUND, options.isUseSound() ? 1 : 0);
    WinRegistry.setIntValue(MAME_REG_FOLDER_KEY + rom, KEY_USE_SAMPLES, options.isUseSamples() ? 1 : 0);
    WinRegistry.setIntValue(MAME_REG_FOLDER_KEY + rom, KEY_DMD_COMPACT, options.isCompactDisplay() ? 1 : 0);
    WinRegistry.setIntValue(MAME_REG_FOLDER_KEY + rom, KEY_DMD_DOUBLE_SIZE, options.isDoubleDisplaySize() ? 1 : 0);
    WinRegistry.setIntValue(MAME_REG_FOLDER_KEY + rom, KEY_IGNORE_ROM_ERRORS, options.isIgnoreRomCrcError() ? 1 : 0);
    WinRegistry.setIntValue(MAME_REG_FOLDER_KEY + rom, KEY_CABINET_MODE, options.isCabinetMode() ? 1 : 0);
    WinRegistry.setIntValue(MAME_REG_FOLDER_KEY + rom, KEY_SHOW_DMD, options.isShowDmd() ? 1 : 0);
    WinRegistry.setIntValue(MAME_REG_FOLDER_KEY + rom, KEY_USER_EXTERNAL_DMD, options.isUseExternalDmd() ? 1 : 0);
    WinRegistry.setIntValue(MAME_REG_FOLDER_KEY + rom, KEY_COLORIZE_DMD, options.isColorizeDmd() ? 1 : 0);
    WinRegistry.setIntValue(MAME_REG_FOLDER_KEY + rom, KEY_SOUND_MODE, options.isSoundMode() ? 1 : 0);
    WinRegistry.setIntValue(MAME_REG_FOLDER_KEY + rom, KEY_FORCE_STEREO, options.isForceStereo() ? 1 : 0);

    mameCache.put(options.getRom().toLowerCase(), options);
    return getOptions(rom);
  }

  public boolean deleteOptions(String rom) {
    WinRegistry.deleteKey(MAME_REG_FOLDER_KEY + rom);
    mameCache.remove(rom.toLowerCase());
    return true;
  }

  private boolean getBoolean(Map<String, Object> values, String key) {
    return values.containsKey(key) && values.get(key) instanceof Integer && (((Integer) values.get(key)) == 1);
  }

  public void installRom(UploadDescriptor uploadDescriptor, File tempFile, UploaderAnalysis analysis) throws IOException {
    GameEmulator gameEmulator = popperService.getGameEmulator(uploadDescriptor.getEmulatorId());
    installMameFile(uploadDescriptor, tempFile, analysis, AssetType.ZIP, gameEmulator.getRomFolder());
  }

  public void installNvRam(UploadDescriptor uploadDescriptor, File tempFile, UploaderAnalysis analysis) throws IOException {
    GameEmulator gameEmulator = popperService.getGameEmulator(uploadDescriptor.getEmulatorId());
    installMameFile(uploadDescriptor, tempFile, analysis, AssetType.NV, gameEmulator.getNvramFolder());
  }

  public void installCfg(UploadDescriptor uploadDescriptor, File tempFile, UploaderAnalysis analysis) throws IOException {
    GameEmulator gameEmulator = popperService.getGameEmulator(uploadDescriptor.getEmulatorId());
    installMameFile(uploadDescriptor, tempFile, analysis, AssetType.CFG, gameEmulator.getCfgFolder());
  }

  public void installMameFile(UploadDescriptor uploadDescriptor, File tempFile, UploaderAnalysis analysis, AssetType assetType, File folder) throws IOException {
    if (analysis == null) {
      analysis = new UploaderAnalysis(tempFile);
      analysis.analyze();
    }

    File out = new File(folder, uploadDescriptor.getOriginalUploadFileName());
    String nvFileName = analysis.getFileNameForAssetType(assetType);
    if (nvFileName != null) {
      out = new File(folder, nvFileName);
      if (out.exists() && !out.delete()) {
        throw new IOException("Failed to delete existing " + assetType.name() + " file " + out.getAbsolutePath());
      }
      ZipUtil.unzipTargetFile(tempFile, out, nvFileName);
    }
    else {
      if (out.exists() && !out.delete()) {
        throw new IOException("Failed to delete existing " + assetType.name() + " file " + out.getAbsolutePath());
      }
      org.apache.commons.io.FileUtils.copyFile(tempFile, out);
      LOG.info("Installed " + assetType.name() + ": " + out.getAbsolutePath());
    }
  }

  @Override
  public void afterPropertiesSet() {
    new Thread(() -> {
      Thread.currentThread().setName("MAME Initializer");
      clearCache();
    }).start();
  }

  public boolean clearCacheFor(@Nullable String rom) {
    if (!StringUtils.isEmpty(rom)) {
      mameCache.remove(rom);
      getOptions(rom);
      return true;
    }
    return false;
  }

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;
  }
}
