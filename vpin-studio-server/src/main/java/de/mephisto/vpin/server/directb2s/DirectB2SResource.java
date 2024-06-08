package de.mephisto.vpin.server.directb2s;

import de.mephisto.vpin.connectors.vps.model.VpsDiffTypes;
import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.directb2s.DirectB2S;
import de.mephisto.vpin.restclient.directb2s.DirectB2SData;
import de.mephisto.vpin.restclient.directb2s.DirectB2STableSettings;
import de.mephisto.vpin.restclient.directb2s.DirectB2ServerSettings;
import de.mephisto.vpin.restclient.games.descriptors.UploadDescriptor;
import de.mephisto.vpin.restclient.games.descriptors.UploadDescriptorFactory;
import de.mephisto.vpin.server.VPinStudioServer;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.games.UniversalUploadService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.DatatypeConverter;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

/**
 *
 */
@RestController
@RequestMapping(VPinStudioServer.API_SEGMENT + "directb2s")
public class DirectB2SResource {
  private final static Logger LOG = LoggerFactory.getLogger(DirectB2SResource.class);

  @Autowired
  private BackglassService backglassService;

  @Autowired
  private GameService gameService;

  @Autowired
  private UniversalUploadService universalUploadService;

  @GetMapping("/{id}")
  public DirectB2SData getData(@PathVariable("id") int gameId) {
    return backglassService.getDirectB2SData(gameId);
  }

  @GetMapping("/background/{emuId}/{filename}")
  public ResponseEntity<Resource> getBackground(@PathVariable("emuId") int emuId, @PathVariable("filename") String filename ) {
    // first decoding done by the RestService but an extra one is needed
    filename = URLDecoder.decode(filename, StandardCharsets.UTF_8);
    return download(backglassService.getBackgroundBase64(emuId, filename), filename, ".png");
  }
  @GetMapping("/dmdimage/{emuId}/{filename}")
  public ResponseEntity<Resource> getDmdImage(@PathVariable("emuId") int emuId, @PathVariable("filename") String filename ) {
    // first decoding done by the RestService but an extra one is needed
    filename = URLDecoder.decode(filename, StandardCharsets.UTF_8);
    return download(backglassService.getDmdBase64(emuId, filename), filename, ".dmd.png");
  }

  protected ResponseEntity<Resource> download(String base64, String filename, String extension) {
    String name = StringUtils.indexOf(filename, '/')>=0? StringUtils.substringAfterLast(filename, "/"): filename;
    name = StringUtils.substringBeforeLast(name, ".") + extension;

    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + name);
    headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
    headers.add("Pragma", "no-cache");
    headers.add("Expires", "0");

    if (base64==null) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    byte[] image = DatatypeConverter.parseBase64Binary(base64);
    ByteArrayResource resource = new ByteArrayResource(image);

    return ResponseEntity.ok()
            .headers(headers)
            .contentLength(resource.contentLength())
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(resource);
  }

  @PostMapping("/get")
  public DirectB2SData getData(@RequestBody DirectB2S directB2S) {
    return backglassService.getDirectB2SData(directB2S);
  }

  @PostMapping("/delete")
  public boolean deleteBackglass(@RequestBody DirectB2S directB2S) {
    return backglassService.deleteBackglass(directB2S.getEmulatorId(), directB2S.getFileName());
  }

  @PutMapping
  public boolean updateBackglass(@RequestBody Map<String, Object> values) throws IOException {
    int emulatorId = (Integer) values.get("emulatorId");
    String fileName = (String) values.get("fileName");
    String newName = (String) values.get("newName");
    if (values.containsKey("newName") && !StringUtils.isEmpty(newName)) {
      return backglassService.rename(emulatorId, fileName, newName);
    }

    if (values.containsKey("duplicate")) {
      return backglassService.duplicate(emulatorId, fileName);
    }
    return false;
  }

  @GetMapping
  public List<DirectB2S> getBackglasses() {
    return backglassService.getBackglasses();
  }

  @GetMapping("/tablesettings/{gameId}")
  public DirectB2STableSettings getTableSettings(@PathVariable("gameId") int gameId) {
    return backglassService.getTableSettings(gameId);
  }

  @PostMapping("/tablesettings/{gameId}")
  public DirectB2STableSettings saveTableSettings(@PathVariable("gameId") int gameId, @RequestBody DirectB2STableSettings settings) {
    try {
      return backglassService.saveTableSettings(gameId, settings);
    } catch (Exception e) {
      throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "Table not supported: " + e.getMessage());
    }
  }

  @GetMapping("/serversettings/{emuId}")
  public DirectB2ServerSettings getServerSettings(@PathVariable("emuId") int emuId) {
    return backglassService.getServerSettings(emuId);
  }

  @PostMapping("/serversettings/{emuId}")
  public DirectB2ServerSettings saveServerSettings(@PathVariable("emuId") int emuId, @RequestBody DirectB2ServerSettings settings) {
    try {
      return backglassService.saveServerSettings(emuId, settings);
    } catch (Exception e) {
      throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "Saving custom options failed: " + e.getMessage());
    }
  }

  @PostMapping("/upload")
  public UploadDescriptor uploadDirectB2s(@RequestParam(value = "file", required = false) MultipartFile file,
                                          @RequestParam("objectId") Integer gameId) {
    UploadDescriptor descriptor = UploadDescriptorFactory.create(file, gameId);
    try {
      descriptor.getAssetsToImport().add(AssetType.DIRECTB2S);
      descriptor.upload();
      universalUploadService.importFileBasedAssets(descriptor, AssetType.DIRECTB2S);
      gameService.resetUpdate(gameId, VpsDiffTypes.b2s);
      return descriptor;
    } catch (Exception e) {
      LOG.error("Directb2s upload failed: " + e.getMessage(), e);
      throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "DirectB2S upload failed: " + e.getMessage());
    }
    finally {
      descriptor.finalizeUpload();
    }
  }
}
