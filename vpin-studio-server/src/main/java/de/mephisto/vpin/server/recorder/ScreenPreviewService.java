package de.mephisto.vpin.server.recorder;

import de.mephisto.vpin.restclient.frontend.FrontendPlayerDisplay;
import de.mephisto.vpin.restclient.recorder.RecordingScreen;
import de.mephisto.vpin.restclient.system.ScreenInfo;
import de.mephisto.vpin.server.util.ImageUtil;
import edu.umd.cs.findbugs.annotations.NonNull;
import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.OutputStream;

@Service
public class ScreenPreviewService implements InitializingBean {
  private final static Logger LOG = LoggerFactory.getLogger(ScreenPreviewService.class);

  public void capture(@NonNull OutputStream out, @NonNull RecordingScreen recordingScreen) {
    try {
      FrontendPlayerDisplay display = recordingScreen.getDisplay();
      Rectangle rectangle = new Rectangle(display.getX(), display.getY(), display.getWidth(), display.getHeight());
      Robot robot = new Robot();
      BufferedImage bufferedImage = robot.createScreenCapture(rectangle);
//      ImageUtil.write(bufferedImage, new File("c:/temp/out.jpg"));
      ImageUtil.writeJPG(bufferedImage, out);
    }
    catch (Exception e) {
      LOG.error("Failed to generated screen capture for " + recordingScreen + ": {}", e.getMessage(), e);
    }
  }

  public void capture(@NonNull OutputStream out, @NonNull ScreenInfo display) {
    try {
      Rectangle rectangle = new Rectangle((int) display.getX(), (int) display.getY(), display.getWidth(), display.getHeight());
      Robot robot = new Robot();
      BufferedImage bufferedImage = robot.createScreenCapture(rectangle);
      ImageUtil.writeJPG(bufferedImage, out);
    }
    catch (Exception e) {
      LOG.error("Failed to generated screen capture for monitor #" + display + ": {}", e.getMessage(), e);
    }
  }

  @Override
  public void afterPropertiesSet() throws Exception {

  }
}
