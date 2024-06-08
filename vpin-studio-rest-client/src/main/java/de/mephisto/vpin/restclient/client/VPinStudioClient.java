package de.mephisto.vpin.restclient.client;

import de.mephisto.vpin.restclient.OverlayClient;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.RestClient;
import de.mephisto.vpin.restclient.altcolor.AltColorServiceClient;
import de.mephisto.vpin.restclient.altsound.AltSoundServiceClient;
import de.mephisto.vpin.restclient.alx.AlxServiceClient;
import de.mephisto.vpin.restclient.archiving.ArchiveServiceClient;
import de.mephisto.vpin.restclient.assets.AssetServiceClient;
import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.cards.HighscoreCardTemplatesServiceClient;
import de.mephisto.vpin.restclient.cards.HighscoreCardsServiceClient;
import de.mephisto.vpin.restclient.competitions.CompetitionRepresentation;
import de.mephisto.vpin.restclient.competitions.CompetitionType;
import de.mephisto.vpin.restclient.competitions.CompetitionsServiceClient;
import de.mephisto.vpin.restclient.components.ComponentServiceClient;
import de.mephisto.vpin.restclient.directb2s.BackglassServiceClient;
import de.mephisto.vpin.restclient.discord.DiscordServer;
import de.mephisto.vpin.restclient.discord.DiscordServiceClient;
import de.mephisto.vpin.restclient.dmd.DMDServiceClient;
import de.mephisto.vpin.restclient.dof.DOFServiceClient;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.games.GameStatusServiceClient;
import de.mephisto.vpin.restclient.games.GamesServiceClient;
import de.mephisto.vpin.restclient.games.NVRamsServiceClient;
import de.mephisto.vpin.restclient.highscores.HigscoreBackupServiceClient;
import de.mephisto.vpin.restclient.highscores.ScoreListRepresentation;
import de.mephisto.vpin.restclient.highscores.ScoreSummaryRepresentation;
import de.mephisto.vpin.restclient.jobs.JobsServiceClient;
import de.mephisto.vpin.restclient.mame.MameServiceClient;
import de.mephisto.vpin.restclient.players.PlayersServiceClient;
import de.mephisto.vpin.restclient.players.RankedPlayerRepresentation;
import de.mephisto.vpin.restclient.popper.PinUPPopperServiceClient;
import de.mephisto.vpin.restclient.popper.PopperScreen;
import de.mephisto.vpin.restclient.preferences.PreferencesServiceClient;
import de.mephisto.vpin.restclient.puppacks.PupPackServiceClient;
import de.mephisto.vpin.restclient.representations.PreferenceEntryRepresentation;
import de.mephisto.vpin.restclient.system.SystemServiceClient;
import de.mephisto.vpin.restclient.textedit.TextEditorServiceClient;
import de.mephisto.vpin.restclient.tournaments.TournamentsServiceClient;
import de.mephisto.vpin.restclient.vpbm.VpbmServiceClient;
import de.mephisto.vpin.restclient.vps.VpsServiceClient;
import de.mephisto.vpin.restclient.vpx.VpxServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;

public class VPinStudioClient implements OverlayClient {
  private final static Logger LOG = LoggerFactory.getLogger(VPinStudioClient.class);
  public final static String API = "api/v1/";

  private final RestClient restClient;

  private final AltSoundServiceClient altSoundServiceClient;
  private final AltColorServiceClient altColorServiceClient;
  private final ArchiveServiceClient archiveServiceClient;
  private final AlxServiceClient alxServiceClient;
  private final AssetServiceClient assetServiceClient;
  private final CompetitionsServiceClient competitions;
  private final ComponentServiceClient componentServiceClient;
  private final BackglassServiceClient backglassServiceClient;
  private final DiscordServiceClient discordServiceClient;
  private final DMDServiceClient dmdServiceClient;
  private final DOFServiceClient dofServiceClient;
  private final GamesServiceClient gamesServiceClient;
  private final GameStatusServiceClient gameStatusServiceClient;
  private final HighscoreCardsServiceClient highscoreCardsServiceClient;
  private final HighscoreCardTemplatesServiceClient highscoreCardTemplatesServiceClient;
  private final HigscoreBackupServiceClient higscoreBackupServiceClient;
  private final ImageCache imageCache;
  private final JobsServiceClient jobsServiceClient;
  private final MameServiceClient mameServiceClient;
  private final NVRamsServiceClient nvRamsServiceClient;
  private final PlayersServiceClient playersServiceClient;
  private final PinUPPopperServiceClient pinUPPopperServiceClient;
  private final PreferencesServiceClient preferencesServiceClient;
  private final PupPackServiceClient pupPackServiceClient;
  private final SystemServiceClient systemServiceClient;
  private final TournamentsServiceClient tournamentsServiceClient;
  private final TextEditorServiceClient textEditorServiceClient;
  private final PinVolServiceClient pinVolServiceClient;
  private final PINemHiServiceClient pinemHiServiceClient;
  private final PlaylistsServiceClient playlistsServiceClient;
  private final VpbmServiceClient vpbmServiceClient;
  private final VpxServiceClient vpxServiceClient;
  private final VpsServiceClient vpsServiceClient;

  public VPinStudioClient(String host) {
    restClient = RestClient.createInstance(host);
    this.preferencesServiceClient = new PreferencesServiceClient(this);

    this.alxServiceClient = new AlxServiceClient(this);
    this.altColorServiceClient = new AltColorServiceClient(this);
    this.altSoundServiceClient = new AltSoundServiceClient(this);
    this.archiveServiceClient = new ArchiveServiceClient(this);
    this.assetServiceClient = new AssetServiceClient(this);
    this.competitions = new CompetitionsServiceClient(this);
    this.componentServiceClient = new ComponentServiceClient(this);
    this.backglassServiceClient = new BackglassServiceClient(this);
    this.dmdServiceClient = new DMDServiceClient(this);
    this.dofServiceClient = new DOFServiceClient(this);
    this.discordServiceClient = new DiscordServiceClient(this);
    this.gamesServiceClient = new GamesServiceClient(this);
    this.gameStatusServiceClient = new GameStatusServiceClient(this);
    this.highscoreCardsServiceClient = new HighscoreCardsServiceClient(this);
    this.highscoreCardTemplatesServiceClient = new HighscoreCardTemplatesServiceClient(this);
    this.imageCache = new ImageCache(this);
    this.jobsServiceClient = new JobsServiceClient(this);
    this.mameServiceClient = new MameServiceClient(this);
    this.nvRamsServiceClient = new NVRamsServiceClient(this);
    this.playersServiceClient = new PlayersServiceClient(this);
    this.pupPackServiceClient = new PupPackServiceClient(this);
    this.pinUPPopperServiceClient = new PinUPPopperServiceClient(this);
    this.systemServiceClient = new SystemServiceClient(this);
    this.textEditorServiceClient = new TextEditorServiceClient(this);
    this.vpxServiceClient = new VpxServiceClient(this);
    this.vpsServiceClient = new VpsServiceClient(this);
    this.vpbmServiceClient = new VpbmServiceClient(this);
    this.pinVolServiceClient = new PinVolServiceClient(this);
    this.pinemHiServiceClient = new PINemHiServiceClient(this);
    this.playlistsServiceClient = new PlaylistsServiceClient(this);
    this.higscoreBackupServiceClient = new HigscoreBackupServiceClient(this);

    this.tournamentsServiceClient = new TournamentsServiceClient(this, preferencesServiceClient);
  }

  public String getSystemPreset() {
    PreferenceEntryRepresentation preference = getPreference(PreferenceNames.SYSTEM_PRESET);
    String preset = preference.getValue();
    if (preset == null) {
      preset = PreferenceNames.SYSTEM_PRESET_64_BIT;
    }
    return preset;
  }

  public HighscoreCardTemplatesServiceClient getHighscoreCardTemplatesClient() {
    return highscoreCardTemplatesServiceClient;
  }

  public TextEditorServiceClient getTextEditorService() {
    return textEditorServiceClient;
  }

  public GameStatusServiceClient getGameStatusService() {
    return gameStatusServiceClient;
  }

  public TournamentsServiceClient getTournamentsService() {
    return tournamentsServiceClient;
  }

  public void setErrorHandler(VPinStudioClientErrorHandler errorHandler) {
    restClient.setErrorHandler(errorHandler);
  }

  public AlxServiceClient getAlxService() {
    return alxServiceClient;
  }

  public DOFServiceClient getDofService() {
    return dofServiceClient;
  }

  public ComponentServiceClient getComponentService() {
    return componentServiceClient;
  }

  public DMDServiceClient getDmdService() {
    return dmdServiceClient;
  }

  public NVRamsServiceClient getNvRamsService() {
    return nvRamsServiceClient;
  }

  public VpsServiceClient getVpsService() {
    return vpsServiceClient;
  }

  public HigscoreBackupServiceClient getHigscoreBackupService() {
    return higscoreBackupServiceClient;
  }

  public PlaylistsServiceClient getPlaylistsService() {
    return playlistsServiceClient;
  }

  public PINemHiServiceClient getPINemHiService() {
    return pinemHiServiceClient;
  }

  public PinVolServiceClient getPinVolService() {
    return pinVolServiceClient;
  }

  public AltColorServiceClient getAltColorService() {
    return altColorServiceClient;
  }

  public MameServiceClient getMameService() {
    return mameServiceClient;
  }

  public PupPackServiceClient getPupPackService() {
    return pupPackServiceClient;
  }

  public AltSoundServiceClient getAltSoundService() {
    return altSoundServiceClient;
  }

  public ArchiveServiceClient getArchiveService() {
    return archiveServiceClient;
  }

  public AssetServiceClient getAssetService() {
    return assetServiceClient;
  }

  public CompetitionsServiceClient getCompetitionService() {
    return competitions;
  }

  public BackglassServiceClient getBackglassServiceClient() {
    return backglassServiceClient;
  }

  public DiscordServiceClient getDiscordService() {
    return discordServiceClient;
  }

  public GamesServiceClient getGameService() {
    return gamesServiceClient;
  }

  public HighscoreCardsServiceClient getHighscoreCardsService() {
    return highscoreCardsServiceClient;
  }

  public ImageCache getImageCache() {
    return imageCache;
  }

  public JobsServiceClient getJobsService() {
    return jobsServiceClient;
  }

  public PlayersServiceClient getPlayerService() {
    return playersServiceClient;
  }

  public PinUPPopperServiceClient getPinUPPopperService() {
    return pinUPPopperServiceClient;
  }

  public PreferencesServiceClient getPreferenceService() {
    return preferencesServiceClient;
  }

  public SystemServiceClient getSystemService() {
    return systemServiceClient;
  }

  public VpxServiceClient getVpxService() {
    return vpxServiceClient;
  }

  public VpbmServiceClient getVpbmService() {
    return vpbmServiceClient;
  }

  @Override
  public DiscordServer getDiscordServer(long serverId) {
    return discordServiceClient.getDiscordServer(serverId);
  }

  @Override
  public List<CompetitionRepresentation> getFinishedCompetitions(int limit) {
    return getCompetitionService().getFinishedCompetitions(limit);
  }

  @Override
  public CompetitionRepresentation getActiveCompetition(CompetitionType type) {
    return getCompetitionService().getActiveCompetition(type);
  }

  @Override
  public GameRepresentation getGame(int id) {
    return getGameService().getGame(id);
  }

  @Override
  public GameRepresentation getGameCached(int id) {
    return getGameService().getGameCached(id);
  }

  @Override
  public InputStream getCachedUrlImage(String url) {
    return getImageCache().getCachedUrlImage(url);
  }

  @Override
  public ScoreSummaryRepresentation getCompetitionScore(long id) {
    return getCompetitionService().getCompetitionScore(id);
  }

  @Override
  public ByteArrayInputStream getCompetitionBackground(long gameId) {
    return getCompetitionService().getCompetitionBackground(gameId);
  }

  @Override
  public ScoreListRepresentation getCompetitionScoreList(long id) {
    return getCompetitionService().getCompetitionScoreList(id);
  }

  @Override
  public ByteArrayInputStream getAsset(AssetType assetType, String uuid) {
    return getAssetService().getAsset(assetType, uuid);
  }

  @Override
  public ScoreSummaryRepresentation getRecentScores(int count) {
    return getGameService().getRecentScores(count);
  }

  @Override
  public ByteArrayInputStream getGameMediaItem(int id, @Nullable PopperScreen screen) {
    return getAssetService().getGameMediaItem(id, screen);
  }

  @Override
  public PreferenceEntryRepresentation getPreference(String key) {
    return this.preferencesServiceClient.getPreference(key);
  }

  @Override
  public List<RankedPlayerRepresentation> getRankedPlayers() {
    return getPlayerService().getRankedPlayers();
  }

  public RestClient getRestClient() {
    return restClient;
  }


  public void clearDiscordCache() {
    restClient.clearCache("discord/");
  }


  /*********************************************************************************************************************
   * Utils
   */
  public void download(@NonNull String url, @NonNull File target) throws Exception {
    RestTemplate template = new RestTemplate();
    LOG.info("HTTP Download " + restClient.getBaseUrl() + VPinStudioClientService.API + url);
    File file = template.execute(restClient.getBaseUrl() + VPinStudioClientService.API + url, HttpMethod.GET, null, clientHttpResponse -> {
      FileOutputStream out = null;
      try {
        out = new FileOutputStream(target);
        StreamUtils.copy(clientHttpResponse.getBody(), out);
        return target;
      } catch (Exception e) {
        throw e;
      } finally {
        out.close();
      }
    });
  }

  public String getURL(String segment) {
    if (!segment.startsWith("http") && !segment.contains(VPinStudioClientService.API)) {
      return restClient.getBaseUrl() + VPinStudioClientService.API + segment;
    }
    return segment;
  }

  public void clearCache() {
    getDiscordService().clearCache();
    getImageCache().clearCache();
    getGameService().clearCache();
    getSystemService().clearCache();
    getPupPackService().clearCache();
    getDmdService().clearCache();
  }

  public void clearWheelCache() {
    getImageCache().clearWheelCache();
  }
}
