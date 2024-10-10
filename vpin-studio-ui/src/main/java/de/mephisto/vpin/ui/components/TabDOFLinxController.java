package de.mephisto.vpin.ui.components;

import de.mephisto.vpin.restclient.components.ComponentType;
import de.mephisto.vpin.restclient.games.GameEmulatorRepresentation;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class TabDOFLinxController extends AbstractComponentTab implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(TabDOFLinxController.class);

  @FXML
  private void onFolder() {
    GameEmulatorRepresentation defaultGameEmulator = client.getFrontendService().getDefaultGameEmulator();
    File folder = new File(defaultGameEmulator.getMameDirectory());
    openFolder(folder);
  }

  @FXML
  private void onReload() {
//    client.getDmdService().clearCache();
    refreshCustomValues();
  }

  private void refreshCustomValues() {
    clearCustomValues();
//
//    ComponentRepresentation freezyComponent = client.getComponentService().getComponent(ComponentType.freezy);
//    Date lastModified = freezyComponent.getLastModified();
//    if (lastModified != null && this.component.getLastModified() != null) {
//      ComponentSummaryEntry entry = new ComponentSummaryEntry();
//      entry.setValid(true);
//      if (lastModified.before(this.component.getLastModified())) {
//        entry.setValue("The Serum .dll files are newer than the Freezy installation files.");
//      }
//      else {
//        entry.setValue("The Freezy installation is newer than the Serum .dll files.");
//      }
//      entry.setName("Serum Status");
//      entry.setDescription("If the Serum .dll files are newer than the Freezy installation, make sure that they are added as plugin with \"passthrough\" set to \"true\".");
//
//      super.addCustomValue(entry);
//    }
  }


  @Override
  protected ComponentType getComponentType() {
    return ComponentType.doflinx;
  }


  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    super.initialize();
    onReload();
  }
}
