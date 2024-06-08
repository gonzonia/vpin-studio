package de.mephisto.vpin.restclient.popper;

import de.mephisto.vpin.connectors.assets.TableAsset;
import de.mephisto.vpin.restclient.DatabaseLockException;
import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.client.VPinStudioClientService;
import de.mephisto.vpin.restclient.games.*;
import de.mephisto.vpin.restclient.games.descriptors.UploadDescriptor;
import de.mephisto.vpin.restclient.jobs.JobExecutionResult;
import de.mephisto.vpin.restclient.util.FileUploadProgressListener;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/*********************************************************************************************************************
 * Popper
 ********************************************************************************************************************/
public class PinUPPopperServiceClient extends VPinStudioClientService {
  private final static Logger LOG = LoggerFactory.getLogger(VPinStudioClient.class);
  private static final int CACHE_SIZE = 300;

  private List<TableAssetSearch> cache = new ArrayList<>();

  public PinUPPopperServiceClient(VPinStudioClient client) {
    super(client);
  }


  public int getVersion() {
    return getRestClient().get(API + "popper/version", Integer.class);
  }

  public GameList getImportableTables() {
    return getRestClient().get(API + "popper/imports", GameList.class);
  }

  public JobExecutionResult importTable(GameListItem item) throws Exception {
    try {
      return getRestClient().post(API + "popper/import", item, JobExecutionResult.class);
    }
    catch (Exception e) {
      LOG.error("Failed importing tables: " + e.getMessage(), e);
      throw e;
    }
  }

  public PinUPControl getPinUPControlFor(PopperScreen screen) {
    return getRestClient().get(API + "popper/pincontrol/" + screen.name(), PinUPControl.class);
  }

  public GameEmulatorRepresentation getGameEmulator(int id) {
    List<GameEmulatorRepresentation> gameEmulators = getGameEmulators();
    return gameEmulators.stream().filter(e -> e.getId() == id).findFirst().orElse(null);
  }

  public GameEmulatorRepresentation getDefaultGameEmulator() {
    List<GameEmulatorRepresentation> gameEmulators = getGameEmulators();
    return gameEmulators.get(0);
  }

  public PinUPPlayerDisplay getScreenDisplay(PopperScreen screen) {
    return getRestClient().get(API + "popper/screen/" + screen.name(), PinUPPlayerDisplay.class);
  }

  public List<PinUPPlayerDisplay> getScreenDisplays() {
    return Arrays.asList(getRestClient().get(API + "popper/screens", PinUPPlayerDisplay[].class));
  }

  public List<GameEmulatorRepresentation> getGameEmulators() {
    return Arrays.asList(getRestClient().getCached(API + "popper/emulators", GameEmulatorRepresentation[].class));
  }

  public List<GameEmulatorRepresentation> getVpxGameEmulators() {
    List<GameEmulatorRepresentation> gameEmulators = getGameEmulators();
    return gameEmulators.stream().filter(e -> e.isVpxEmulator()).collect(Collectors.toList());
  }

  public List<GameEmulatorRepresentation> getGameEmulatorsUncached() {
    return Arrays.asList(getRestClient().get(API + "popper/emulators", GameEmulatorRepresentation[].class));
  }

  public List<GameEmulatorRepresentation> getBackglassGameEmulators() {
    return Arrays.asList(getRestClient().getCached(API + "popper/backglassemulators", GameEmulatorRepresentation[].class));
  }

  public PinUPControls getPinUPControls() {
    return getRestClient().get(API + "popper/pincontrols", PinUPControls.class);
  }

  public boolean isPinUPPopperRunning() {
    return getRestClient().get(API + "popper/running", Boolean.class);
  }

  public boolean terminatePopper() {
    return getRestClient().get(API + "popper/terminate", Boolean.class);
  }

  public boolean restartPopper() {
    return getRestClient().get(API + "popper/restart", Boolean.class);
  }

  public TableDetails getTableDetails(int gameId) {
    return getRestClient().get(API + "popper/tabledetails/" + gameId, TableDetails.class);
  }

  public TableDetails saveTableDetails(TableDetails tableDetails, int gameId) throws Exception {
    try {
      return getRestClient().post(API + "popper/tabledetails/" + gameId, tableDetails, TableDetails.class);
    }
    catch (Exception e) {
      LOG.error("Failed save table details: " + e.getMessage(), e);
      throw e;
    }
  }

  public TableDetails autoFillTableDetails(int gameId, boolean overwrite) throws Exception {
    try {
      return getRestClient().put(API + "popper/tabledetails/autofill/" + gameId + "/" + overwrite, Collections.emptyMap(), TableDetails.class);
    }
    catch (Exception e) {
      LOG.error("Failed autofilling table details: " + e.getMessage(), e);
      throw e;
    }
  }

  public TableDetails autoFillTableDetails(int gameId, TableDetails tableDetails) throws Exception {
    try {
      return getRestClient().post(API + "popper/tabledetails/autofillsimulate/" + gameId, tableDetails, TableDetails.class);
    }
    catch (Exception e) {
      LOG.error("Failed simulating autofilling table details: " + e.getMessage(), e);
      throw e;
    }
  }

  public TableDetails autoMatch(int gameId, boolean overwrite) {
    return getRestClient().get(API + "popper/tabledetails/automatch/" + gameId + "/" + overwrite, TableDetails.class);
  }

  //-----------------------------

  public PopperCustomOptions saveCustomOptions(PopperCustomOptions options) throws Exception {
    try {
      return getRestClient().post(API + "popper/custompoptions", options, PopperCustomOptions.class);
    }
    catch (HttpClientErrorException e) {
      if (e.getStatusCode().is4xxClientError()) {
        throw new DatabaseLockException(e);
      }
      throw e;
    }
    catch (Exception e) {
      LOG.error("Failed save custom options: " + e.getMessage(), e);
      throw e;
    }
  }

  public PopperCustomOptions getCustomOptions() {
    return getRestClient().get(API + "popper/custompoptions", PopperCustomOptions.class);
  }

  public boolean deleteMedia(int gameId, PopperScreen screen, String name) {
    return getRestClient().delete(API + "poppermedia/media/" + gameId + "/" + screen.name() + "/" + name);
  }


  public boolean renameMedia(int gameId, PopperScreen screen, String name, String newName) throws Exception {
    Map<String, Object> params = new HashMap<>();
    params.put("oldName", name);
    params.put("newName", newName);
    return getRestClient().put(API + "poppermedia/media/" + gameId + "/" + screen.name(), params, Boolean.class);
  }

  public boolean toFullScreen(int gameId, PopperScreen screen) throws Exception {
    try {
      Map<String, Object> values = new HashMap<>();
      values.put("fullscreen", "true");
      return getRestClient().put(API + "poppermedia/media/" + gameId + "/" + screen.name(), values);
    }
    catch (Exception e) {
      LOG.error("Applying fullscreen mode failed: " + e.getMessage(), e);
      throw e;
    }
  }

  public boolean addBlank(int gameId, PopperScreen screen) throws Exception {
    try {
      Map<String, Object> values = new HashMap<>();
      values.put("blank", "true");
      return getRestClient().put(API + "poppermedia/media/" + gameId + "/" + screen.name(), values);
    }
    catch (Exception e) {
      LOG.error("Adding blank asset failed: " + e.getMessage(), e);
      throw e;
    }
  }

  public GameMediaRepresentation getGameMedia(int gameId) {
    return getRestClient().get(API + "poppermedia/" + gameId, GameMediaRepresentation.class);
  }


  public JobExecutionResult uploadMedia(File file, int gameId, PopperScreen screen, FileUploadProgressListener listener) throws Exception {
    try {
      String url = getRestClient().getBaseUrl() + API + "poppermedia/upload/" + screen.name();
      HttpEntity upload = createUpload(file, gameId, null, AssetType.POPPER_MEDIA, listener);
      ResponseEntity<JobExecutionResult> exchange = new RestTemplate().exchange(url, HttpMethod.POST, upload, JobExecutionResult.class);
      finalizeUpload(upload);
      return exchange.getBody();
    }
    catch (Exception e) {
      LOG.error("Popper media upload failed: " + e.getMessage(), e);
      throw e;
    }
  }


  public UploadDescriptor uploadPack(File file, int gameId, FileUploadProgressListener listener) throws Exception {
    try {
      String url = getRestClient().getBaseUrl() + API + "poppermedia/packupload";
      HttpEntity upload = createUpload(file, gameId, null, AssetType.POPPER_MEDIA, listener);
      ResponseEntity<UploadDescriptor> exchange = new RestTemplate().exchange(url, HttpMethod.POST, upload, UploadDescriptor.class);
      finalizeUpload(upload);
      return exchange.getBody();
    }
    catch (Exception e) {
      LOG.error("Popper media upload failed: " + e.getMessage(), e);
      throw e;
    }
  }

  //---------------- Assets---------------------------------------------------------------------------------------------

  public synchronized TableAssetSearch getCached(PopperScreen screen, String term) {
    for (TableAssetSearch s : this.cache) {
      if (s.getTerm().equals(term) && s.getScreen().equals(screen)) {
        return s;
      }
    }

    if (!StringUtils.isEmpty(term) && term.trim().contains(" ")) {
      term = term.split(" ")[0];
      for (TableAssetSearch s : this.cache) {
        if (s.getTerm().equals(term) && s.getScreen().equals(screen)) {
          return s;
        }
      }
    }

    return null;
  }

  public synchronized TableAssetSearch searchTableAsset(PopperScreen screen, String term) throws Exception {
    term = term.replaceAll("/", "");
    term = term.replaceAll("&", " ");
    term = term.replaceAll(",", " ");

    TableAssetSearch cached = getCached(screen, term);
    if (cached != null) {
      return cached;
    }

    TableAssetSearch search = new TableAssetSearch();
    search.setTerm(term);
    search.setScreen(screen);
    TableAssetSearch result = getRestClient().post(API + "poppermedia/assets/search", search, TableAssetSearch.class);
    if (result != null) {
      if (result.getResult().isEmpty() && !StringUtils.isEmpty(term) && term.trim().contains(" ")) {
        String[] split = term.trim().split(" ");
        return searchTableAsset(screen, split[0]);
      }

      cache.add(result);
      if (cache.size() > CACHE_SIZE) {
        cache.remove(0);
      }
      return result;
    }
    return search;
  }

  public boolean downloadTableAsset(TableAsset tableAsset, PopperScreen screen, GameRepresentation game, boolean append) throws Exception {
    try {
      return getRestClient().post(API + "poppermedia/assets/download/" + game.getId() + "/" + screen.name() + "/" + append, tableAsset, Boolean.class);
    }
    catch (Exception e) {
      LOG.error("Failed to save b2s server settings: " + e.getMessage(), e);
      throw e;
    }
  }

  public void clearCache() {
    getRestClient().clearCache("popper/emulators");
    this.cache.clear();
  }
}
