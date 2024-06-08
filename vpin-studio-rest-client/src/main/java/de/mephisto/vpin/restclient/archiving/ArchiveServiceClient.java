package de.mephisto.vpin.restclient.archiving;

import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.client.VPinStudioClientService;
import de.mephisto.vpin.restclient.jobs.JobExecutionResult;
import de.mephisto.vpin.restclient.games.descriptors.ArchiveBundleDescriptor;
import de.mephisto.vpin.restclient.games.descriptors.ArchiveCopyToRepositoryDescriptor;
import de.mephisto.vpin.restclient.games.descriptors.ArchiveRestoreDescriptor;
import de.mephisto.vpin.restclient.games.descriptors.BackupDescriptor;
import de.mephisto.vpin.restclient.util.FileUploadProgressListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/*********************************************************************************************************************
 * Archiving
 ********************************************************************************************************************/
public class ArchiveServiceClient extends VPinStudioClientService {
  private final static Logger LOG = LoggerFactory.getLogger(VPinStudioClient.class);

  public ArchiveServiceClient(VPinStudioClient client) {
    super(client);
  }

  public List<ArchiveDescriptorRepresentation> getArchiveDescriptors(long id) {
    return Arrays.asList(getRestClient().get(API + "archives/" + id, ArchiveDescriptorRepresentation[].class));
  }

  public List<ArchiveDescriptorRepresentation> getArchiveDescriptorsFiltered() {
    return Arrays.asList(getRestClient().get(API + "archives/filtered", ArchiveDescriptorRepresentation[].class));
  }

  public List<ArchiveSourceRepresentation> getArchiveSources() {
    return Arrays.asList(getRestClient().get(API + "archives/sources", ArchiveSourceRepresentation[].class));
  }

  public void deleteArchive(long sourceId, String filename) {
    getRestClient().delete(API + "archives/descriptor/" + sourceId + "/" + filename);
  }

  public void deleteArchiveSource(long id) {
    getRestClient().delete(API + "archives/source/" + id);
  }

  public ArchiveSourceRepresentation saveArchiveSource(ArchiveSourceRepresentation source) throws Exception {
    try {
      return getRestClient().post(API + "archives/save", source, ArchiveSourceRepresentation.class);
    } catch (Exception e) {
      LOG.error("Failed to save archive source: " + e.getMessage(), e);
      throw e;
    }
  }

  public List<ArchiveDescriptorRepresentation> getArchiveDescriptorsForGame(int gameId) {
    return Arrays.asList(getRestClient().get(API + "archives/game/" + gameId, ArchiveDescriptorRepresentation[].class));
  }

  public boolean invalidateArchiveCache() {
    return getRestClient().get(API + "archives/invalidate", Boolean.class);
  }

  public JobExecutionResult uploadArchive(File file, int repositoryId, FileUploadProgressListener listener) throws Exception {
    try {
      String url = getRestClient().getBaseUrl() + API + "archives/upload/";
      HttpEntity upload = createUpload(file, repositoryId, null, AssetType.ARCHIVE, listener);
      JobExecutionResult body = createUploadTemplate().exchange(url, HttpMethod.POST, upload, JobExecutionResult.class).getBody();
      finalizeUpload(upload);
      return body;
    } catch (Exception e) {
      LOG.error("Archive upload failed: " + e.getMessage(), e);
      throw e;
    }
  }

  public boolean installArchive(ArchiveDescriptorRepresentation descriptor) throws Exception {
    try {
      return getRestClient().post(API + "archives/install", descriptor, Boolean.class);
    } catch (Exception e) {
      LOG.error("Failed install archive: " + e.getMessage(), e);
      throw e;
    }
  }


  public boolean backupTable(BackupDescriptor exportDescriptor) throws Exception {
    return getRestClient().post(API + "io/backup", exportDescriptor, Boolean.class);
  }

  public boolean installTable(ArchiveRestoreDescriptor descriptor) throws Exception {
    return getRestClient().post(API + "io/install", descriptor, Boolean.class);
  }

  public boolean copyToRepository(ArchiveCopyToRepositoryDescriptor descriptor) throws Exception {
    return getRestClient().post(API + "io/copytorepository", descriptor, Boolean.class);
  }

  public String bundle(ArchiveBundleDescriptor descriptor) throws Exception {
    final RestTemplate restTemplate = new RestTemplate();
    return restTemplate.postForObject(getRestClient().getBaseUrl() + API + "io/bundle", descriptor, String.class);
  }
}
