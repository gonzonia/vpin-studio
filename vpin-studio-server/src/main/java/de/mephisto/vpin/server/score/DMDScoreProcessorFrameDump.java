package de.mephisto.vpin.server.score;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple Processor that writes frames in a file
 */
public class DMDScoreProcessorFrameDump implements DMDScoreProcessor {

  private final static Logger LOG = LoggerFactory.getLogger(DMDScoreProcessorFrameDump.class);

  private Writer writer;

  @Override
  public void onFrameStart(String gameName) {
    try {
      File file = new File("c:/temp/" + gameName + "/dump.txt");
      file.getParentFile().mkdirs(); 
      this.writer = new BufferedWriter(new FileWriter(file));
      writer.append(gameName + "\n\n");
    } 
    catch (IOException ioe) {
    }
  }

  @Override
  public String onFrameReceived(Frame frame, int[] palette, int width, int height) {
    StringBuilder bld = new StringBuilder();
    try {
      writer.append(frame.getType() + " / " + frame.getTimeStamp() + "\n");

      byte[] plane  = frame.getPlane();
      for (int j = 0; j < height; j++) {
        for (int i = 0; i < width; i++) {
          int idx = plane[j * width + i];
          bld.append(idx == 0? " " : idx<=9 ? Integer.toString(idx) : Character.toString(55 + idx));
        }
        bld.append("\n");
      }

      writer.append(bld);
      writer.append("\n");
      writer.flush();
    }
    catch (IOException ioe) {
      LOG.error("error while writing frame");
    }
    return bld.toString();
  }

  @Override
  public void onFrameStop(String gameName) {
    try {
      this.writer.close();
    }
    catch (IOException ioe) {
      LOG.error("error while opening writer");
    }
  }
}
