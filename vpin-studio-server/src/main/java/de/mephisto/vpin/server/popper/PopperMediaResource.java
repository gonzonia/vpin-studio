package de.mephisto.vpin.server.popper;

import de.mephisto.vpin.commons.utils.FileUtils;
import de.mephisto.vpin.connectors.assets.EncryptDecrypt;
import de.mephisto.vpin.connectors.assets.TableAsset;
import de.mephisto.vpin.connectors.assets.TableAssetsAdapter;
import de.mephisto.vpin.connectors.assets.TableAssetsService;
import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.games.descriptors.UploadDescriptor;
import de.mephisto.vpin.restclient.games.descriptors.UploadDescriptorFactory;
import de.mephisto.vpin.restclient.jobs.JobExecutionResult;
import de.mephisto.vpin.restclient.jobs.JobExecutionResultFactory;
import de.mephisto.vpin.restclient.popper.PopperScreen;
import de.mephisto.vpin.restclient.popper.TableAssetSearch;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.games.UniversalUploadResource;
import de.mephisto.vpin.server.games.UniversalUploadService;
import de.mephisto.vpin.server.system.SystemService;
import de.mephisto.vpin.server.util.UploadUtil;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;
import static de.mephisto.vpin.server.util.RequestUtil.CONTENT_LENGTH;
import static de.mephisto.vpin.server.util.RequestUtil.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequestMapping(API_SEGMENT + "poppermedia")
public class PopperMediaResource implements InitializingBean {
  public static final byte[] EMPTY_MP4 = Base64.getDecoder().decode("AAAAGGZ0eXBpc29tAAAAAGlzb21tcDQxAAAACGZyZWUAAAAmbWRhdCELUCh9wBQ+4cAhC1AAfcAAPuHAIQtQAH3AAD7hwAAAAlNtb292AAAAbG12aGQAAAAAxzFHd8cxR3cAAV+QAAAYfQABAAABAAAAAAAAAAAAAAAAAQAAAAAAAAAAAAAAAAAAAAEAAAAAAAAAAAAAAAAAAEAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAADAAAAG2lvZHMAAAAAEA0AT////xX/DgQAAAACAAABxHRyYWsAAABcdGtoZAAAAAfHMUd3xzFHdwAAAAIAAAAAAAAYfQAAAAAAAAAAAAAAAAEAAAAAAQAAAAAAAAAAAAAAAAAAAAEAAAAAAAAAAAAAAAAAAEAAAAAAAAAAAAAAAAAAAWBtZGlhAAAAIG1kaGQAAAAAxzFHd8cxR3cAAKxEAAAL/xXHAAAAAAA0aGRscgAAAAAAAAAAc291bgAAAAAAAAAAAAAAAFNvdW5kIE1lZGlhIEhhbmRsZXIAAAABBG1pbmYAAAAQc21oZAAAAAAAAAAAAAAAJGRpbmYAAAAcZHJlZgAAAAAAAAABAAAADHVybCAAAAABAAAAyHN0YmwAAABkc3RzZAAAAAAAAAABAAAAVG1wNGEAAAAAAAAAAQAAAAAAAAAAAAIAEAAAAACsRAAAAAAAMGVzZHMAAAAAA4CAgB8AQBAEgICAFEAVAAYAAAANdQAADXUFgICAAhIQBgECAAAAGHN0dHMAAAAAAAAAAQAAAAMAAAQAAAAAHHN0c2MAAAAAAAAAAQAAAAEAAAADAAAAAQAAABRzdHN6AAAAAAAAAAoAAAADAAAAFHN0Y28AAAAAAAAAAQAAACg=");
  public static final byte[] EMPTY_MP3 = Base64.getDecoder().decode("SUQzAwAAAAADJVRGTFQAAAAPAAAB//5NAFAARwAvADMAAABDT01NAAAAggAAAGRldWlUdW5TTVBCACAwMDAwMDAwMCAwMDAwMDAwMCAwMDAwMDAwMCAwMDAwMDAwMDAwMDAxMmMxIDAwMDAwMDAwIDAwMDAwMDAwIDAwMDAwMDAwIDAwMDAwMDAwIDAwMDAwMDAwIDAwMDAwMDAwIDAwMDAwMDAwIDAwMDAwMDAwAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAD/+7RAAAAE4ABLgAAACAAACXAAAAEAAAEuAAAAIAAAJcAAAAT/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////+7RAwAAP/ABLgAAACByACXAAAAEAAAEuAAAAIAAAJcAAAAT/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////+7RAwAAP/ABLgAAACByACXAAAAEAAAEuAAAAIAAAJcAAAAT/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////+7RAwAAP/ABLgAAACByACXAAAAEAAAEuAAAAIAAAJcAAAAT///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////8=");
  private final static Logger LOG = LoggerFactory.getLogger(PopperResource.class);

  @Autowired
  private GameService gameService;

  @Autowired
  private UniversalUploadService universalUploadService;

  @Autowired
  private PopperMediaService popperMediaService;

  private TableAssetsService tableAssetsService;

  @GetMapping("/{id}")
  public GameMedia getGameMedia(@PathVariable("id") int id) {
    Game game = gameService.getGame(id);
    if (game == null) {
      throw new ResponseStatusException(NOT_FOUND, "Not game found for id " + id);
    }
    return game.getGameMedia();
  }

  @PostMapping("/assets/search")
  public TableAssetSearch searchTableAssets(@RequestBody TableAssetSearch search) {
    try {
      List<TableAsset> results = tableAssetsService.search(EncryptDecrypt.KEY, search.getScreen().getSegment(), search.getTerm());
      search.setResult(results);
    }
    catch (Exception e) {
      LOG.error("Asset search failed: " + e.getMessage(), e);
    }
    return search;
  }

  @PostMapping("/assets/download/{gameId}/{screen}/{append}")
  public boolean downloadTableAsset(@PathVariable("gameId") int gameId,
                                    @PathVariable("screen") String screen,
                                    @PathVariable("append") boolean append,
                                    @RequestBody TableAsset asset) {
    LOG.info("Starting download of " + asset.getName() + "(appending: " + append + ")");
    Game game = gameService.getGame(gameId);
    PopperScreen s = PopperScreen.valueOf(screen);
    File pinpuSystemFolder = game.getPinUPMediaFolder(s);
    File target = new File(pinpuSystemFolder, game.getGameName() + "." + asset.getFileSuffix());
    if (target.exists() && append) {
      target = FileUtils.uniquePopperAsset(target);
    }
    tableAssetsService.download(asset, target);
    return true;
  }

  @GetMapping("/{id}/{screen}/{name}")
  public ResponseEntity<Resource> handleRequestWithName(@PathVariable("id") int id, @PathVariable("screen") String screen, @PathVariable("name") String name) throws IOException {
    PopperScreen popperScreen = PopperScreen.valueOf(screen);
    Game game = gameService.getGame(id);
    if (game != null) {
      GameMedia gameMedia = game.getGameMedia();
      GameMediaItem gameMediaItem = gameMedia.getDefaultMediaItem(popperScreen);
      if (!StringUtils.isEmpty(name)) {
        name = name.replaceAll("%(?![0-9a-fA-F]{2})", "%25");
        name = name.replaceAll("\\+", "%2B");
        name = URLDecoder.decode(name, Charset.defaultCharset());
        gameMediaItem = gameMedia.getMediaItem(popperScreen, name);
      }

      if (gameMediaItem != null) {
        File file = gameMediaItem.getFile();
        FileInputStream in = new FileInputStream(file);
        byte[] bytes = IOUtils.toByteArray(in);
        ByteArrayResource bytesResource = new ByteArrayResource(bytes);
        in.close();

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set(CONTENT_LENGTH, String.valueOf(file.length()));
        responseHeaders.set(CONTENT_TYPE, gameMediaItem.getMimeType());
        responseHeaders.set("Access-Control-Allow-Origin", "*");
        responseHeaders.set("Access-Control-Expose-Headers", "origin, range");
        responseHeaders.set("Cache-Control", "public, max-age=3600");
        return ResponseEntity.ok().headers(responseHeaders).body(bytesResource);
      }
    }

    return ResponseEntity.notFound().build();
  }

  @GetMapping("/{id}/{screen}")
  public ResponseEntity<Resource> handleRequest(@PathVariable("id") int id, @PathVariable("screen") String screen) throws IOException {
    return handleRequestWithName(id, screen, null);
  }

  @PostMapping("/upload/{screen}")
  public JobExecutionResult upload(@PathVariable("screen") PopperScreen popperScreen,
                                   @RequestParam(value = "file", required = false) MultipartFile file,
                                   @RequestParam("objectId") Integer gameId) {
    try {
      if (file == null) {
        LOG.error("Upload request did not contain a file object.");
        return JobExecutionResultFactory.error("Upload request did not contain a file object.");
      }

      Game game = gameService.getGame(gameId);
      if (game == null) {
        LOG.error("No game found for popper media upload.");
        return JobExecutionResultFactory.error("No game found for PinUP Popper media upload.");
      }

      String suffix = FilenameUtils.getExtension(file.getOriginalFilename());
      File out = popperMediaService.uniquePopperAsset(game, popperScreen, suffix);
      LOG.info("Uploading " + out.getAbsolutePath());
      UploadUtil.upload(file, out);

      return JobExecutionResultFactory.empty();
    }
    catch (Exception e) {
      throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "ALT sound upload failed: " + e.getMessage());
    }
  }

  @PostMapping("/packupload")
  public UploadDescriptor uploadPack(@RequestParam(value = "file", required = false) MultipartFile file,
                                     @RequestParam("objectId") Integer gameId) {
    UploadDescriptor descriptor = UploadDescriptorFactory.create(file, gameId);
    try {
      descriptor.getAssetsToImport().add(AssetType.POPPER_MEDIA);
      descriptor.upload();
      universalUploadService.importArchiveBasedAssets(descriptor, null, AssetType.POPPER_MEDIA);
      return descriptor;
    }
    catch (Exception e) {
      LOG.error(AssetType.POPPER_MEDIA.name() + " upload failed: " + e.getMessage(), e);
      throw new ResponseStatusException(INTERNAL_SERVER_ERROR, AssetType.POPPER_MEDIA.name() + " upload failed: " + e.getMessage());
    } finally {
      descriptor.finalizeUpload();
    }
  }

  @DeleteMapping("/media/{gameId}/{screen}/{file}")
  public boolean deleteMedia(@PathVariable("gameId") int gameId, @PathVariable("screen") PopperScreen screen, @PathVariable("file") String filename) {
    Game game = gameService.getGame(gameId);
    File pinUPMediaFolder = game.getPinUPMediaFolder(screen);
    File media = new File(pinUPMediaFolder, filename);
    if (media.exists()) {
      return media.delete();
    }
    return false;
  }

  @PutMapping("/media/{gameId}/{screen}")
  public boolean doPut(@PathVariable("gameId") int gameId, @PathVariable("screen") PopperScreen screen, @RequestBody Map<String, String> data) throws Exception {
    try {
      if (data.containsKey("fullscreen")) {
        return toFullscreenMedia(gameId, screen);
      }
      if (data.containsKey("blank")) {
        return addBlank(gameId, screen);
      }
      if (data.containsKey("oldName")) {
        return renameAsset(gameId, screen, data.get("oldName"), data.get("newName"));
      }
      return true;
    }
    catch (Exception e) {
      LOG.error("Failed to execute media change request: " + e.getMessage(), e);
    }
    return false;
  }

  private boolean renameAsset(int gameId, PopperScreen screen, String oldName, String newName) {
    Game game = gameService.getGame(gameId);
    List<File> pinUPMedia = game.getPinUPMedia(screen);
    for (File file : pinUPMedia) {
      if (file.getName().equals(oldName)) {
        File renamed = new File(file.getParentFile(), newName);
        if (file.renameTo(renamed)) {
          LOG.info("Renamed \"" + file.getAbsolutePath() + "\" to \"" + renamed.getAbsolutePath() + "\"");
          return true;
        }
      }
    }
    return false;
  }

  private boolean toFullscreenMedia(int gameId, PopperScreen screen) throws IOException {
    Game game = gameService.getGame(gameId);
    List<File> pinUPMedia = game.getPinUPMedia(screen);
    if (pinUPMedia.size() == 1) {
      File mediaFile = pinUPMedia.get(0);
      String name = mediaFile.getName();
      String baseName = FilenameUtils.getBaseName(name);
      String suffix = FilenameUtils.getExtension(name);
      String updatedBaseName = baseName + "(SCREEN3)." + suffix;

      LOG.info("Renaming " + mediaFile.getAbsolutePath() + " to '" + updatedBaseName + "'");
      boolean renamed = mediaFile.renameTo(new File(mediaFile.getParentFile(), updatedBaseName));
      if (!renamed) {
        LOG.error("Renaming to " + updatedBaseName + " failed.");
        return false;
      }

      File target = new File(mediaFile.getParentFile(), name);

      LOG.info("Copying blank asset to " + target.getAbsolutePath());
      FileOutputStream out = new FileOutputStream(target);
      //copy base64 encoded 0s video
      IOUtils.write(EMPTY_MP4, out);
      out.close();

      return true;
    }
    return false;
  }

  private boolean addBlank(int gameId, PopperScreen screen) throws IOException {
    Game game = gameService.getGame(gameId);
    File target = popperMediaService.uniquePopperAsset(game, screen);
    FileOutputStream out = new FileOutputStream(target);
    //copy base64 asset
    if (screen.equals(PopperScreen.AudioLaunch) || screen.equals(PopperScreen.Audio)) {
      IOUtils.write(EMPTY_MP3, out);
    }
    else {
      IOUtils.write(EMPTY_MP4, out);
    }
    LOG.info("Written blank asset \"" + target.getAbsolutePath() + "\"");
    out.close();
    return true;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    tableAssetsService = new TableAssetsService();

    try {
      Class<?> aClass = Class.forName("de.mephisto.vpin.popper.PopperAssetAdapter");
      TableAssetsAdapter adapter = (TableAssetsAdapter) aClass.getDeclaredConstructor().newInstance();
      tableAssetsService.registerAdapter(adapter);
    }
    catch (Exception e) {
      LOG.error("Unable to find PopperAssetAdapter: " + e.getMessage());
    }
  }
}
