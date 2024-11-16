package de.mephisto.vpin.server.util;

import de.mephisto.vpin.server.VPinStudioServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class VersionUtil {
  private final static Logger LOG = LoggerFactory.getLogger(VersionUtil.class);

  public static String getVersion() {
    try {
      final Properties properties = new Properties();
      InputStream resourceAsStream = VPinStudioServer.class.getClassLoader().getResourceAsStream("version.properties");
      properties.load(resourceAsStream);
      resourceAsStream.close();
      return properties.getProperty("vpin.studio.version");
    } catch (IOException e) {
      LOG.error("Failed to read version number: " + e.getMessage(), e);
    }
    return null;
  }
}
