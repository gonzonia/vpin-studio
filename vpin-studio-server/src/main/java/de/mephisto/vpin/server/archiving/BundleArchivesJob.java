package de.mephisto.vpin.server.archiving;

import de.mephisto.vpin.commons.utils.FileUtils;
import de.mephisto.vpin.restclient.jobs.Job;
import de.mephisto.vpin.restclient.jobs.JobExecutionResult;
import de.mephisto.vpin.restclient.games.descriptors.ArchiveBundleDescriptor;
import de.mephisto.vpin.server.system.SystemService;
import de.mephisto.vpin.commons.utils.ZipUtil;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipOutputStream;

public class BundleArchivesJob implements Job {
  private final static Logger LOG = LoggerFactory.getLogger(BundleArchivesJob.class);

  private final ArchiveService archiveService;
  private final SystemService systemService;
  private final ArchiveBundleDescriptor archiveBundleDescriptor;
  private final List<ArchiveDescriptor> archiveDescriptors;

  private String status;
  private int processed;

  private File temp;
  private File target;
  private File tempFile;

  public BundleArchivesJob(@NonNull ArchiveService archiveService,
                           @NonNull SystemService systemService,
                           @NonNull ArchiveBundleDescriptor archiveBundleDescriptor,
                           @NonNull List<ArchiveDescriptor> archiveDescriptors) {
    this.archiveService = archiveService;
    this.systemService = systemService;
    this.archiveBundleDescriptor = archiveBundleDescriptor;
    this.archiveDescriptors = archiveDescriptors;
  }

  @Override
  public JobExecutionResult execute() {
    JobExecutionResult result = new JobExecutionResult();

    FileOutputStream fos = null;
    ZipOutputStream zipOut = null;
    long start = System.currentTimeMillis();

    String prefix = FilenameUtils.getBaseName(archiveDescriptors.get(0).getFilename()).replaceAll(" ", "-");
    String targetName = prefix + "-bundle-" + System.currentTimeMillis() + ".zip";
    target = new File(archiveService.getArchivesFolder(), targetName);
    File tempFile = new File(archiveService.getArchivesFolder(), targetName + ".bak");

    List<ArchiveDescriptor> exportedDescriptors = new ArrayList<>();

    try {
      status = "Initializing...";

      LOG.info("Creating temporary bundle file " + tempFile.getAbsolutePath());
      fos = new FileOutputStream(tempFile);
      zipOut = new ZipOutputStream(fos);
      File descriptorFolder = new File(archiveDescriptors.get(0).getSource().getLocation());

      for (ArchiveDescriptor archiveDescriptor : archiveDescriptors) {
        status = "Adding " + archiveDescriptor.getFilename() + " to bundle.";

        File exportedArchive = archiveService.export(archiveDescriptor);
        if (exportedArchive != null && exportedArchive.exists()) {
          ZipUtil.zipFile(exportedArchive, exportedArchive.getName(), zipOut);
          LOG.info("Zipping " + exportedArchive.getAbsolutePath());

          File descriptor = new File(descriptorFolder, FilenameUtils.getBaseName(archiveDescriptor.getFilename()) + ".json");
          if (descriptor.exists()) {
            LOG.info("Zipping " + descriptor.getAbsolutePath());
            ZipUtil.zipFile(descriptor, descriptor.getName(), zipOut);
          }
          else {
            LOG.info("Descriptor file " + descriptor.getAbsolutePath() + " not found for bundling.");
          }
          exportedDescriptors.add(archiveDescriptor);
        }

        processed++;
      }

      File descriptorTemp = File.createTempFile("descriptor", ".json");
      descriptorTemp.deleteOnExit();
      ArchiveUtil.exportDescriptorJson(exportedDescriptors, descriptorTemp);
      ZipUtil.zipFile(descriptorTemp, ArchiveUtil.DESCRIPTOR_JSON, zipOut);
      descriptorTemp.delete();
    } catch (Exception e) {
      LOG.error("Bundle creation failed: " + e.getMessage(), e);
      result.setError("Bundle creation failed: " + e.getMessage());
      return result;
    }
    try {
      if (zipOut != null) {
        zipOut.close();
      }
    } catch (IOException e) {
      //ignore
    }

    try {
      if (fos != null) {
        fos.close();
      }
    } catch (IOException e) {
      //ignore
    }
    boolean renamed = tempFile.renameTo(target);
    if (renamed) {
      LOG.info("Finished creating bundle " + target.getAbsolutePath() + ", took " + ((System.currentTimeMillis() - start) / 1000) + " seconds, " + FileUtils.readableFileSize(target.length()));
    }
    else {
      LOG.error("Final renaming export file to " + target.getAbsolutePath() + " failed.");
      result.setError("Final renaming export file to " + target.getAbsolutePath() + " failed.");
    }
    return result;
  }

  public File getTarget() {
    return target;
  }

  @Override
  public double getProgress() {
    if (processed == 0) {
      return 1;
    }
    long currentSize = processed;
    return currentSize * 100d / archiveDescriptors.size();
  }

  @Override
  public String getStatus() {
    return status;
  }
}
