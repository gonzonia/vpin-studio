package de.mephisto.vpin.ui.tables.vps;


import de.mephisto.vpin.connectors.vps.model.VpsTableVersion;
import de.mephisto.vpin.ui.tournaments.VpsVersionContainer;
import javafx.scene.control.ListCell;

public class VpsTableVersionCell extends ListCell<VpsTableVersion> {
  public VpsTableVersionCell() {
  }

  protected void updateItem(VpsTableVersion item, boolean empty) {
    super.updateItem(item, empty);
    setGraphic(null);
    setText(null);
    if (item != null) {
      setGraphic(new VpsVersionContainer(item, "", false));
    }
  }
}