package de.mephisto.vpin.ui.util;

import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.DataFormat;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class FileDragEventHandler implements EventHandler<DragEvent> {

  private final Node node;
  private final boolean singleSelectionOnly;
  private List<String> suffixes;

  public FileDragEventHandler(Node node, boolean singleSelectionOnly, String... suffix) {
    this.node = node;
    this.singleSelectionOnly = singleSelectionOnly;
    this.suffixes = Arrays.asList(suffix);
  }

  @Override
  public void handle(DragEvent event) {
    List<File> files = event.getDragboard().getFiles();

    Set<DataFormat> contentTypes = event.getDragboard().getContentTypes();
    if (contentTypes.isEmpty()) {
      return;
    }

    boolean containsMedia = !files.isEmpty();
//    for (DataFormat contentType : contentTypes) {
//      if (checkDataFormat(contentType)) {
//        containsMedia = true;
//      }
//    }
//
//    if (!containsMedia) {
//      return;
//    }

    //files may be empty for drag from a zip file
    if (!files.isEmpty() && singleSelectionOnly && files.size() > 1) {
      return;
    }

    if (suffixes != null) {
      for (File file : files) {
        if (file.length() == 0) {
          continue;
        }

        String extension = FilenameUtils.getExtension(file.getName());
        if (!suffixes.contains(extension)) {
          return;
        }
      }
    }

    if (event.getGestureSource() != node && containsMedia) {
      event.acceptTransferModes(TransferMode.COPY);
    }
    else {
      event.consume();
    }
  }

  private boolean checkDataFormat(DataFormat contentType) {
    Set<String> identifiers = contentType.getIdentifiers();
    for (String identifier : identifiers) {
      for (String suffix : suffixes) {
        if (identifier.toLowerCase().contains("." + suffix.toLowerCase())) {
          return true;
        }
      }
    }
    return false;
  }
}
