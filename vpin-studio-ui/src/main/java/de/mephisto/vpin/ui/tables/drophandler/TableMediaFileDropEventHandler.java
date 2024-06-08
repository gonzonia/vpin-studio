package de.mephisto.vpin.ui.tables.drophandler;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.popper.PopperScreen;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.tables.TableOverviewController;
import de.mephisto.vpin.ui.tables.dialogs.TableAssetManagerDialogController;
import de.mephisto.vpin.ui.tables.dialogs.TableMediaUploadProgressModel;
import de.mephisto.vpin.ui.util.ProgressDialog;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.input.DragEvent;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TableMediaFileDropEventHandler implements EventHandler<DragEvent> {
  private final static Logger LOG = LoggerFactory.getLogger(TableMediaFileDropEventHandler.class);

  private final PopperScreen screen;
  private final List<String> suffixes;
  private TableOverviewController tablesController;
  private TableAssetManagerDialogController dialogController;

  public TableMediaFileDropEventHandler(TableOverviewController tablesController, PopperScreen screen, String... suffix) {
    this.tablesController = tablesController;
    this.screen = screen;
    this.suffixes = Arrays.asList(suffix);
  }

  public TableMediaFileDropEventHandler(TableAssetManagerDialogController dialogController, PopperScreen screen, String... suffix) {
    this.dialogController = dialogController;
    this.screen = screen;
    this.suffixes = Arrays.asList(suffix);
  }

  @Override
  public void handle(DragEvent event) {
    List<File> files = event.getDragboard().getFiles();
    List<File> filtered = files.stream().filter(f -> {
      String suffix = FilenameUtils.getExtension(f.getName());
      return suffixes.contains(suffix);
    }).collect(Collectors.toList());

    if (filtered.isEmpty()) {
      Platform.runLater(() -> {
        WidgetFactory.showAlert(Studio.stage, "Error", "None of the selected is valid for this upload.",
            "Only files with extension(s) \"" + String.join("\", \"", suffixes) + "\" are accepted here.");
      });
      return;
    }

    List<File> draggedCopies = new ArrayList<>();
    try {
      for (File file : filtered) {
        String baseName = FilenameUtils.getBaseName(file.getName());
        String suffix = FilenameUtils.getExtension(file.getName());
        File copy = File.createTempFile(baseName, "." + suffix);
        FileUtils.copyFile(file, copy);
        draggedCopies.add(copy);
        LOG.info("Writted dropped copy: " + copy.getAbsolutePath());
      }
    }
    catch (IOException e) {
      LOG.info("Creating drop copies failed: " + e.getMessage(), e);
      Platform.runLater(() -> {
        WidgetFactory.showAlert(Studio.stage, "Error", "Creating copies from drop failed: " + e.getMessage());
      });
      return;
    }


    Platform.runLater(() -> {
      GameRepresentation game = null;
      if (this.tablesController != null) {
        game = tablesController.getSelection();
      }
      else {
        game = dialogController.getGame();
      }

      TableMediaUploadProgressModel model = new TableMediaUploadProgressModel(game.getId(),
          "Popper Media Upload", draggedCopies, screen);
      ProgressDialog.createProgressDialog(model);

      if (dialogController != null) {
        dialogController.refreshTableMediaView();
      }
    });
  }
}
