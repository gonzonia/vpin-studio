package de.mephisto.vpin.tablemanager.states;

import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.games.descriptors.ArchiveRestoreDescriptor;
import de.mephisto.vpin.restclient.popper.Playlist;
import de.mephisto.vpin.restclient.archiving.ArchiveDescriptorRepresentation;
import de.mephisto.vpin.tablemanager.Menu;
import de.mephisto.vpin.tablemanager.MenuController;
import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class InstallingMenuState extends MenuState {
  private final static Logger LOG = LoggerFactory.getLogger(VPinStudioClient.class);
  private final MenuState parentState;
  private final MenuController menuController;
  private final Playlist playlist;

  public InstallingMenuState(MenuState parentState, MenuController menuController, Playlist playlist) {
    this.parentState = parentState;
    this.menuController = menuController;
    this.playlist = playlist;
    this.menuController.enterInstalling();
    this.menuController.setNameLabelText("Restoring, please wait...");

    StateMananger.getInstance().setInputBlocked(true);
    executeInstallation();
  }

  @Override
  MenuState left() {
    return this;
  }

  @Override
  MenuState right() {
    return this;
  }

  @Override
  MenuState enter() {
    return this;
  }

  @Override
  MenuState back() {
    menuController.leaveConfirmation();
    return parentState.back();
  }

  private void executeInstallation() {
    Platform.runLater(() -> {
      this.menuController.showProgressbar();
    });

    new Thread(() -> {
      ArchiveDescriptorRepresentation archiveDescriptor = this.menuController.getArchiveSelection();

      ArchiveRestoreDescriptor descriptor = new ArchiveRestoreDescriptor();
      descriptor.setArchiveSourceId(archiveDescriptor.getSource().getId());
      descriptor.setFilename(archiveDescriptor.getFilename());

//      List<Playlist> playlists = Menu.client.getPlaylistsService().getStaticPlaylists();
//      if (playlist != null) {
//        descriptor.setPlaylistId(playlist.getId());
//      }
//      else if (!playlists.isEmpty()) {
//        descriptor.setPlaylistId(playlists.get(0).getId());
//      }

      try {
        Menu.client.getArchiveService().installTable(descriptor);
      } catch (Exception e) {
        LOG.error("Failed to executing installation: " + e.getMessage(), e);
      }

      StateMananger.getInstance().waitForJobAndGoBack();
    }).start();

  }
}
