package de.mephisto.vpin.server.frontend.pinballx;

import de.mephisto.vpin.restclient.JsonSettings;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.alx.TableAlxEntry;
import de.mephisto.vpin.restclient.frontend.*;
import de.mephisto.vpin.restclient.frontend.pinballx.PinballXSettings;
import de.mephisto.vpin.restclient.util.FileUtils;
import de.mephisto.vpin.restclient.validation.GameValidationCode;
import de.mephisto.vpin.server.frontend.BaseConnector;
import de.mephisto.vpin.server.frontend.GameEntry;
import de.mephisto.vpin.server.playlists.Playlist;
import de.mephisto.vpin.server.system.SystemService;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.configuration2.INIConfiguration;
import org.apache.commons.configuration2.SubnodeConfiguration;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

import static de.mephisto.vpin.commons.SystemInfo.RESOURCES;

@Service("PinballX")
public class PinballXConnector extends BaseConnector {
  private final static Logger LOG = LoggerFactory.getLogger(PinballXConnector.class);

  public final static String PINBALL_X = FrontendType.PinballX.name();

  @Autowired
  private SystemService systemService;

  @Autowired
  //private PinballXAssetsAdapter assetsAdapter;
  private PinballXAssetsIndexAdapter assetsAdapter;

  private Map<String, TableDetails> mapTableDetails = new HashMap<>();


  @Override
  public void initializeConnector() {
    super.setMediaAccessStrategy(new PinballXMediaAccessStrategy(getInstallationFolder()));

    PinballXSettings ps = getSettings();
    if (ps != null && ps.isGameExEnabled()) {
      assetsAdapter.configureCredentials(ps.getGameExMail(), ps.getGameExPassword());
      super.setTableAssetAdapter(assetsAdapter);
      // no effect if already started
      this.assetsAdapter.startRefresh();
    }
    else {
      super.setTableAssetAdapter(null);
      // no effect if already stopped
      this.assetsAdapter.stopRefresh();
    }
    LOG.info("Finished initialization of " + this);
  }

  @NonNull
  @Override
  public File getInstallationFolder() {
    return systemService.getPinballXInstallationFolder();
  }

  public Frontend getFrontend() {
    Frontend frontend = new Frontend();
    frontend.setName("PinballX");
    frontend.setInstallationDirectory(getInstallationFolder().getAbsolutePath());
    frontend.setFrontendType(FrontendType.PinballX);

    frontend.setFrontendExe(getFrontendExe());
    frontend.setAdminExe("Settings.exe");
    frontend.setIconName("pinballx.png");
    List<VPinScreen> screens = new ArrayList<>(Arrays.asList(VPinScreen.values()));
    screens.remove(VPinScreen.Other2);
    frontend.setSupportedScreens(screens);
    frontend.setIgnoredValidations(Arrays.asList(GameValidationCode.CODE_NO_OTHER2,
        GameValidationCode.CODE_PUP_PACK_FILE_MISSING,
        GameValidationCode.CODE_ALT_SOUND_FILE_MISSING
    ));

    // recordings screens
    frontend.setSupportedRecordingScreens(Arrays.asList(VPinScreen.PlayField, VPinScreen.BackGlass,
        VPinScreen.DMD, VPinScreen.Menu, VPinScreen.Topper));

    frontend.setPlayfieldMediaInverted(true);
    return frontend;
  }

  @Override
  public PinballXSettings getSettings() {
    try {
      return preferencesService.getJsonPreference(PreferenceNames.PINBALLX_SETTINGS, PinballXSettings.class);
    }
    catch (Exception e) {
      LOG.error("Getting pinballX settings failed: " + e.getMessage(), e);
      return null;
    }
  }

  @Override
  public void saveSettings(@NonNull Map<String, Object> data) {
    try {
      PinballXSettings settings = JsonSettings.objectMapper.convertValue(data, PinballXSettings.class);
      preferencesService.savePreference(PreferenceNames.PINBALLX_SETTINGS, settings);
      // reinitialize the connector with updated settings
      initializeConnector();
    }
    catch (Exception e) {
      LOG.error("Saving pinballX settings failed: " + e.getMessage(), e);
    }
  }

  @Override
  public void reloadCache() {
    this.mapTableDetails.clear();
    super.reloadCache();
  }

  @Override
  protected List<Emulator> loadEmulators() {
    INIConfiguration iniConfiguration = loadPinballXIni();
    if (iniConfiguration == null) {
      return Collections.emptyList();
    }

    File pinballXFolder = getInstallationFolder();

    List<Emulator> emulators = new ArrayList<>();
    mapTableDetails = new HashMap<>();

    // check standard emulators, starts with Visual Pinball as default one
    String[] emuNames = new String[]{
        "Visual Pinball", "Future Pinball", "Zaccaria", "Pinball FX2", "Pinball FX3", "Pinball Arcade"
    };

    int emuId = 1;
    for (String emuName : emuNames) {
      String sectionName = emuName.replaceAll(" ", "");
      SubnodeConfiguration s = iniConfiguration.getSection(sectionName);
      if (!s.isEmpty()) {
        Emulator emu = createEmulator(s, pinballXFolder, emuId, emuName);
        if (emu != null) {
          emulators.add(emu);
        }
        emuId++;
      }
    }
    // Add specific ones
    for (int k = 1; k < 20; k++) {
      SubnodeConfiguration s = iniConfiguration.getSection("System_" + k);
      if (!s.isEmpty()) {
        String emuname = s.getString("Name");
        Emulator emulator = createEmulator(s, pinballXFolder, emuId++, emuname);
        if (emulator != null) {
          emulators.add(emulator);
        }
      }
    }

    //check the launch and exist scripts
    for (Emulator emulator : emulators) {
      initVisualPinballXScripts(emulator, iniConfiguration);
    }

    return emulators;
  }

  @NonNull
  private File getPinballXIni() {
    File pinballXFolder = getInstallationFolder();
    return new File(pinballXFolder, "/Config/PinballX.ini");
  }

  private INIConfiguration loadPinballXIni() {
    File pinballXIni = getPinballXIni();
    if (!pinballXIni.exists()) {
      LOG.warn("Ini file not found " + pinballXIni);
      return null;
    }

    // mind pinballX.ini is encoded in UTF-16
    INIConfiguration ini = loadIni(pinballXIni, "UTF-16");
    if (ini == null) {
      // ...but old version could be in UTF-8
      ini = loadIni(pinballXIni, "UTF-8");
    }

    return ini;
  }

  private INIConfiguration loadIni(File pinballXIni, String charset) {
    INIConfiguration iniConfiguration = new INIConfiguration();
    //iniConfiguration.setCommentLeadingCharsUsedInInput(";");
    iniConfiguration.setSeparatorUsedInOutput("=");
    //iniConfiguration.setSeparatorUsedInInput("=");

    try (FileReader fileReader = new FileReader(pinballXIni, Charset.forName(charset))) {
      iniConfiguration.read(fileReader);
    }
    catch (Exception e) {
      LOG.error("cannot parse ini file " + pinballXIni, e);
      return null;
    }

    // check presence of [internal] section
    SubnodeConfiguration s = iniConfiguration.getSection("Display");
    return s.isEmpty() ? null : iniConfiguration;
  }

  /*
  [System_1]
  Name=System1 - other VPX
  Enabled=True
  SystemType=1
  WorkingPath=C:\Visual Pinball 10.7
  TablePath=C:\Visual Pinball\tables
  Executable=VPinballX.exe
  Parameters=-light
  */
  private Emulator createEmulator(SubnodeConfiguration s, File installDir, int emuId, String emuname) {
    boolean enabled = s.getBoolean("Enabled", true);
    String tablePath = s.getString("TablePath");
    String workingPath = s.getString("WorkingPath");
    String executable = s.getString("Executable");
    String parameters = s.getString("Parameters");

    if (tablePath == null || !new File(tablePath).exists()) {
      LOG.warn("Skipped loading of \"" + emuname + "\" because the tablePath is invalid");
      return null;
    }

    if (workingPath == null || !new File(workingPath).exists()) {
      LOG.warn("Skipped loading of \"" + emuname + "\" because the workingPath is invalid");
      return null;
    }

    EmulatorType type = null;
    if (s.containsKey("SystemType")) {
      int systemType = s.getInt("SystemType");
      switch (systemType) {
        case 1:
          type = EmulatorType.VisualPinball;
          break; // Visual Pinball
        case 2:
          type = EmulatorType.FuturePinball;
          break; // Future Pinball
        default:
          type = EmulatorType.OTHER;
          break; // Custom Exe
      }
    }
    else {
      type = EmulatorType.fromName(emuname);
    }

    Emulator e = new Emulator(type);
    e.setId(emuId);
    e.setName(emuname);
    e.setDisplayName(emuname);

    File mediaDir = new File(installDir, "Media/" + emuname);
    if (mediaDir.exists() && mediaDir.isDirectory()) {
      e.setDirMedia(mediaDir.getAbsolutePath());
    }

    e.setDirGames(tablePath);
    e.setEmuLaunchDir(workingPath);
    e.setExeName(executable);
    e.setExeParameters(parameters);

    e.setGamesExt(type.getExtension());
    e.setVisible(enabled);

    return e;
  }


  @Override
  protected List<String> loadGames(Emulator emu) {
    File pinballXFolder = getInstallationFolder();
    List<String> games = new ArrayList<>();

    File pinballXDb = new File(pinballXFolder, "/Databases/" + emu.getName() + "/" + emu.getName() + ".xml");
    if (pinballXDb.exists()) {
      PinballXTableParser parser = new PinballXTableParser();
      parser.addGames(pinballXDb, games, mapTableDetails, emu);
    }

    return games;
  }

  @Override
  public int importGame(int emulatorId, @NonNull String gameName, @NonNull String gameFileName,
                        @NonNull String gameDisplayName, @Nullable String launchCustomVar, @NonNull java.util.Date dateFileUpdated) {

    // pinballX does not support gameName, so force equality with gameFileName
    String gameNameFromFileName = gameFileName;
    return super.importGame(emulatorId, gameNameFromFileName, gameFileName, gameDisplayName, launchCustomVar, dateFileUpdated);
  }

  //---------------------------------------------------

  public static String compose(int emuId, String game) {
    return emuId + "@" + game;
  }

  @Override
  protected TableDetails getGameFromDb(int emuId, String game) {
    return mapTableDetails.get(compose(emuId, game));
  }

  @Override
  protected void updateGameInDb(int emuId, String game, TableDetails details) {
    // force gameName = gameFileName
    String gameName = FilenameUtils.getBaseName(details.getGameFileName());
    details.setGameName(gameName);
    mapTableDetails.put(compose(emuId, game), details);
  }

  @Override
  protected void dropGameFromDb(int emuId, String game) {
    mapTableDetails.remove(compose(emuId, game));
  }

  @Override
  protected void commitDb(Emulator emu) {
    File pinballXFolder = getInstallationFolder();
    File pinballXDb = new File(pinballXFolder, "/Databases/" + emu.getName() + "/" + emu.getName() + ".xml");

    PinballXTableParser parser = new PinballXTableParser();
    parser.writeGames(pinballXDb, gamesByEmu.get(emu.getId()), mapTableDetails, emu);
  }

  //------------------------------------------------------------

  @Override
  public List<FrontendPlayerDisplay> getFrontendPlayerDisplays() {
    INIConfiguration iniConfiguration = loadPinballXIni();
    List<FrontendPlayerDisplay> displayList = new ArrayList<>();
    if (iniConfiguration != null) {
      createPlayfieldDisplay(iniConfiguration, displayList);
      createDisplay(iniConfiguration, displayList, "BackGlass", VPinScreen.BackGlass, true);
      createDisplay(iniConfiguration, displayList, "DMD", VPinScreen.DMD, false);
      createDisplay(iniConfiguration, displayList, "Topper", VPinScreen.Topper, false);
      createDisplay(iniConfiguration, displayList, "Apron", VPinScreen.Menu, false);
    }
    return displayList;
  }

  private void createPlayfieldDisplay(INIConfiguration iniConfiguration, List<FrontendPlayerDisplay> players) {
    SubnodeConfiguration display = iniConfiguration.getSection("Display");
    int monitor = Integer.parseInt(display.getString("Monitor", display.getString("monitor", "0")));
    GraphicsDevice[] gds = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
    // sort by xPosition
    Arrays.sort(gds, (g1, g2) -> g1.getDefaultConfiguration().getBounds().x - g2.getDefaultConfiguration().getBounds().x);

    if (monitor < gds.length) {
      java.awt.Rectangle bounds = gds[monitor].getDefaultConfiguration().getBounds();
      int mX = (int) bounds.getX();
      int mY = (int) bounds.getY();

      FrontendPlayerDisplay player = new FrontendPlayerDisplay();
      player.setName(VPinScreen.PlayField.name());
      player.setScreen(VPinScreen.PlayField);
      player.setMonitor(monitor);
      player.setRotation(Integer.parseInt(display.getString("Rotate", "0")));
      player.setInverted(true);

      boolean windowed = display.getBoolean("Windowed", false);
      if (windowed) {
        player.setX(mX + Integer.parseInt(display.getString("windowx", "0")));
        player.setY(mY + Integer.parseInt(display.getString("windowy", "0")));
        player.setWidth(Integer.parseInt(display.getString("windowwidth", "0")));
        player.setHeight(Integer.parseInt(display.getString("windowheight", "0")));
      }
      else {
        player.setX(mX);
        player.setY(mY);
        player.setWidth((int) bounds.getWidth());
        player.setHeight((int) bounds.getHeight());
      }

      LOG.info("Created PinballX player display {}", player);

      players.add(player);
    }
  }

  private void createDisplay(INIConfiguration iniConfiguration, List<FrontendPlayerDisplay> players, String sectionName, VPinScreen screen, boolean defaultEnabled) {
    SubnodeConfiguration display = iniConfiguration.getSection(sectionName);
    if (!display.isEmpty()) {
      boolean enabled = display.getBoolean("Enabled", defaultEnabled);

      int monitor = Integer.parseInt(display.getString("Monitor", display.getString("monitor", "0")));
      GraphicsDevice[] gds = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();

      if (enabled && monitor < gds.length) {
        Rectangle bounds = gds[monitor].getDefaultConfiguration().getBounds();
        int mX = (int) bounds.getX();
        int mY = (int) bounds.getY();

        FrontendPlayerDisplay player = new FrontendPlayerDisplay();
        player.setName(sectionName);
        player.setScreen(screen);
        player.setMonitor(monitor);
        player.setX(mX + Integer.parseInt(display.getString("x", "0")));
        player.setY(mY + Integer.parseInt(display.getString("y", "0")));
        player.setWidth(Integer.parseInt(display.getString("width", "0")));
        player.setHeight(Integer.parseInt(display.getString("height", "0")));

        LOG.info("Created PinballX player display {}", player);
        players.add(player);
      }
    }
  }

  //----------------------------------
  // Playlist management

  @Override
  public boolean deletePlaylist(int playlistId) {
    List<Playlist> playlists = this.loadPlayLists();
    playlists = playlists.stream().filter(p -> p.getId() == playlistId).collect(Collectors.toList());

    File pinballXFolder = getInstallationFolder();
    for (Emulator emu : emulators.values()) {
      File dbfolder = new File(pinballXFolder, "/Databases/" + emu.getName());
      for (File f : dbfolder.listFiles((dir, name) -> StringUtils.endsWithIgnoreCase(name, ".xml"))) {
        String playlistname = FilenameUtils.getBaseName(f.getName());

        for (Playlist playlist : playlists) {
          if (playlist.getName().equals(playlistname)) {
            if (!f.delete()) {
              LOG.info("Failed to delete PinballX playlist {}", f.getAbsolutePath());
            }
            else {
              LOG.info("Deleted PinballX playlist {}", f.getAbsolutePath());
            }
          }
        }
      }
    }
    return super.deletePlaylist(playlistId);
  }

  @Override
  public List<Playlist> loadPlayLists() {
    File pinballXFolder = getInstallationFolder();

    List<Playlist> result = new ArrayList<>();

    int id = 1;
    for (Emulator emu : emulators.values()) {
      File dbfolder = new File(pinballXFolder, "/Databases/" + emu.getName());
      for (File f : dbfolder.listFiles((dir, name) -> StringUtils.endsWithIgnoreCase(name, ".xml"))) {
        String playlistname = FilenameUtils.getBaseName(f.getName());
        if (!StringUtils.equalsIgnoreCase(playlistname, emu.getName())) {

          Playlist playlist = new Playlist();
          playlist.setId(id++);
          playlist.setEmulatorId(emu.getId());
          playlist.setName(playlistname);
          // don't set mediaName, studio will use the name

          PinballXTableParser parser = new PinballXTableParser();
          List<String> _games = new ArrayList<>();
          Map<String, TableDetails> _tabledetails = new HashMap<>();
          parser.addGames(f, _games, _tabledetails, emu);

          List<PlaylistGame> pg = _games.stream()
              .map(g -> toPlaylistGame(findIdFromFilename(emu.getId(), g)))
              .collect(Collectors.toList());
          playlist.setGames(pg);

          result.add(playlist);
        }
      }
    }
    return result;
  }

  @Override
  protected void savePlaylistGame(int gameId, Playlist pl) {
    if (pl.getEmulatorId() != null) {
      Emulator emu = getEmulator(pl.getEmulatorId());
      PinballXTableParser parser = new PinballXTableParser();
      List<GameEntry> games = pl.getGames().stream().map(pg -> getGameEntry(pg.getId())).collect(Collectors.toList());

      File pinballXFolder = getInstallationFolder();
      File playlistDb = new File(pinballXFolder, "/Databases/" + emu.getName() + "/" + pl.getName() + ".xml");
      parser.writeGames(playlistDb, games, mapTableDetails, emu);
    }
  }

  @Override
  public Playlist savePlaylist(Playlist playlist) {
    try {
      if (playlist.getEmulatorId() != null) {
        Emulator emulator = getEmulator(playlist.getEmulatorId());
        File frontendInstallationFolder = getInstallationFolder();
        File dbfolder = new File(frontendInstallationFolder, "/Databases/" + emulator.getName());

        if (playlist.getId() == -1) {
          String name = FileUtils.replaceWindowsChars(playlist.getName());
          File playlistFile = new File(dbfolder, name + ".xml");

          org.apache.commons.io.FileUtils.write(playlistFile, "<menu>\n</menu>", StandardCharsets.UTF_8);
          LOG.info("Written new playlist file {}", playlistFile.getAbsolutePath());
        }
        else {
          Playlist existingPlaylist = getPlaylist(playlist.getId());
          if (existingPlaylist != null) {
            File existingPlaylistFile = new File(dbfolder, existingPlaylist.getName() + ".xml");
            File updated = new File(dbfolder, playlist.getName() + ".xml");
            if (!existingPlaylistFile.renameTo(updated)) {
              LOG.error("Renaming playlist {} to {} failed.", existingPlaylistFile.getAbsolutePath(), updated.getAbsolutePath());
            }
            else {
              LOG.info("Renamed playlist {} to {}.", existingPlaylistFile.getAbsolutePath(), updated.getAbsolutePath());
            }
            deletePlaylistConf(existingPlaylist);
          }
        }
      }
    }
    catch (Exception e) {
      LOG.error("Failed to save PinballX/Y playlist: {}", e.getMessage(), e);
    }
    // now save colors and refresh cache
    return super.savePlaylist(playlist);
  }

//----------------------------------
// Favorites

  @Override
  public Set<Integer> loadFavorites() {
    PinballXStatisticsParser parser = new PinballXStatisticsParser(this);
    Set<Integer> favs = new HashSet<>();
    parser.getAlxData(emulators.values(), null, favs);
    return favs;
  }

  @Override
  protected void saveFavorite(int gameId, boolean favorite) {
    PinballXStatisticsParser parser = new PinballXStatisticsParser(this);
    parser.writeFavorite(getGame(gameId), favorite);
  }

//----------------------------------
// Statistics

  @Override
  public List<TableAlxEntry> loadStats() {
    PinballXStatisticsParser parser = new PinballXStatisticsParser(this);
    List<TableAlxEntry> stats = new ArrayList<>();
    parser.getAlxData(emulators.values(), stats, null);
    return stats;
  }

  @Override
  public boolean updateNumberOfPlaysForGame(int gameId, long value) {
    PinballXStatisticsParser parser = new PinballXStatisticsParser(this);
    parser.writeNumberOfPlayed(getGame(gameId), value);
    return super.updateNumberOfPlaysForGame(gameId, value);
  }

  @Override
  public boolean updateSecondsPlayedForGame(int gameId, long seconds) {
    PinballXStatisticsParser parser = new PinballXStatisticsParser(this);
    parser.writeSecondsPlayed(getGame(gameId), seconds);
    return super.updateSecondsPlayedForGame(gameId, seconds);
  }

//----------------------------------
// UI Management


  @Override
  protected String getFrontendExe() {
    return "PinballX.exe";
  }

//---------------- Utilities -----------------------------------------------------------------------------------------

  private void initVisualPinballXScripts(Emulator emulator, INIConfiguration iniConfiguration) {
    if (emulator.getType().isVpxEmulator()) {
      //VPX scripts
      SubnodeConfiguration visualPinball = iniConfiguration.getSection("VisualPinball");
      visualPinball.setProperty("LaunchBeforeEnabled", "True");
      visualPinball.setProperty("LaunchBeforeExecutable", "emulator-launch.bat");
      visualPinball.setProperty("LaunchBeforeParameters", "\"[TABLEPATH]\\[TABLEFILE]\"");
      visualPinball.setProperty("LaunchBeforeWorkingPath", new File(RESOURCES + "/scripts").getAbsolutePath());

      visualPinball.setProperty("LaunchAfterEnabled", "True");
      visualPinball.setProperty("LaunchAfterExecutable", "emulator-exit.bat");
      visualPinball.setProperty("LaunchAfterParameters", "\"[TABLEPATH]\\[TABLEFILE]\"");
      visualPinball.setProperty("LaunchAfterWorkingPath", new File(RESOURCES + "/scripts").getAbsolutePath());

      //frontend launch script
      SubnodeConfiguration startup = iniConfiguration.getSection("StartupProgram");
      startup.setProperty("Enabled", " True");
      startup.setProperty("Executable", "frontend-launch.bat");
      startup.setProperty("WorkingPath", new File(RESOURCES + "/scripts").getAbsolutePath());

      saveIni(iniConfiguration);
    }
  }

  private void saveIni(INIConfiguration iniConfiguration) {
    if (isAdministrationRunning()) {
      killAdministration();
    }

    try (FileWriter fileWriter = new FileWriter(getPinballXIni(), Charset.forName("UTF-16"))) {
      iniConfiguration.write(fileWriter);
    }
    catch (Exception e) {
      LOG.error("Failed to write PinballX.ini: " + e.getMessage(), e);
    }
  }

  private boolean isAdministrationRunning() {
    List<ProcessHandle> allProcesses = systemService.getProcesses();
    for (ProcessHandle p : allProcesses) {
      if (p.info().command().isPresent()) {
        String cmdName = p.info().command().get();
        if (cmdName.contains("Settings.exe")) {
          return true;
        }
      }
    }
    return false;
  }

  public boolean killAdministration() {
    List<ProcessHandle> processes = ProcessHandle
        .allProcesses()
        .filter(p -> p.info().command().isPresent() && p.info().command().get().contains("Settings.exe"))
        .collect(Collectors.toList());

    if (processes.isEmpty()) {
      LOG.info("No PinballX processes found, termination canceled.");
      return false;
    }

    for (ProcessHandle p : processes) {
      String cmd = p.info().command().get();
      boolean b = p.destroyForcibly();
      LOG.info("Destroyed process '" + cmd + "', result: " + b);
    }
    return true;
  }
}
