package de.mephisto.vpin.ui.launcher;

import de.mephisto.vpin.commons.ServerInstallationUtil;
import de.mephisto.vpin.commons.fx.LoadingOverlayController;
import de.mephisto.vpin.commons.fx.UIDefaults;
import de.mephisto.vpin.commons.utils.CommonImageUtil;
import de.mephisto.vpin.commons.utils.PropertiesStore;
import de.mephisto.vpin.commons.utils.Updater;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.representations.PreferenceEntryRepresentation;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.util.Dialogs;
import de.mephisto.vpin.ui.util.ProgressDialog;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static de.mephisto.vpin.ui.Studio.client;

public class LauncherController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(LauncherController.class);

  @FXML
  private Label studioLabel;

  @FXML
  private Label versionLabel;

  @FXML
  private Button connectBtn;

  @FXML
  private Button refreshBtn;

  @FXML
  private Button installBtn;

  @FXML
  private Button newConnectionBtn;

  @FXML
  private BorderPane main;

  @FXML
  private TableColumn<VPinConnection, String> avatarColumn;

  @FXML
  private TableColumn<VPinConnection, String> nameColumn;

  @FXML
  private TableColumn<VPinConnection, String> hostColumn;

  @FXML
  private TableColumn<VPinConnection, String> actionColumn;

  @FXML
  private TableView<VPinConnection> tableView;

  @FXML
  private HBox installContainer;

  @FXML
  private Hyperlink helpBtn;

  private ObservableList<VPinConnection> data;

  private Stage stage;
  private PropertiesStore store;

  @FXML
  private void onHelp() {
    if (Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
      try {
        Desktop.getDesktop().browse(new URI("https://github.com/syd711/vpin-studio/wiki/Troubleshooting"));
      } catch (Exception ex) {
        LOG.error("Failed to open link: " + ex.getMessage(), ex);
      }
    }
  }

  @FXML
  private void onInstall() {
    boolean install = Dialogs.openInstallerDialog();
    if (install) {
      installServer();
    }
  }

  @FXML
  private void onConnectionRefresh() {
    refreshBtn.setDisable(true);
    connectBtn.setDisable(true);
    newConnectionBtn.setDisable(true);

    stage.getScene().setCursor(Cursor.WAIT);
    new Thread(() -> {
      List<VPinConnection> result = new ArrayList<>();

      VPinConnection connection = checkConnection("localhost");
      if (connection != null) {
        result.add(connection);
      }

      List<Object> entries = store.getEntries();
      for (Object entry : entries) {
        LOG.info("Checking connection to " + entry);
        connection = checkConnection(String.valueOf(entry));
        if (connection != null) {
          result.add(connection);
        }
      }

      Platform.runLater(() -> {
        stage.getScene().setCursor(Cursor.DEFAULT);
        data.clear();
        data.addAll(result);

        refreshBtn.setDisable(false);
        newConnectionBtn.setDisable(false);
      });
    }).start();
  }

  @FXML
  private void onNewConnection() {
    String host = WidgetFactory.showInputDialog(stage, "New VPin Studio Connection", "IP or Hostname", "Enter the IP address or the hostname to connect to.", null, null);
    if (!StringUtils.isEmpty(host)) {
      refreshBtn.setDisable(true);
      connectBtn.setDisable(true);
      newConnectionBtn.setDisable(true);

      stage.getScene().setCursor(Cursor.WAIT);
      new Thread(() -> {
        VPinConnection connection = checkConnection(host);
        Platform.runLater(() -> {
          stage.getScene().setCursor(Cursor.DEFAULT);
          data.clear();
          if (connection != null) {
            store.set(String.valueOf(store.getEntries().size()), host);
            data.add(connection);
          }
          else {
            WidgetFactory.showAlert(stage, "No service found for '" + host + "'.", "Please check the IP or hostname and try again.");
          }

          refreshBtn.setDisable(false);
          newConnectionBtn.setDisable(false);
        });
      }).start();
    }
  }


  @FXML
  private void onConnect() {
    VPinConnection selectedItem = tableView.getSelectionModel().getSelectedItem();
    VPinStudioClient client = new VPinStudioClient(selectedItem.getHost());
    String clientVersion = Studio.getVersion();
    String serverVersion = client.getSystemService().getVersion();

    if (serverVersion != null) {
      if (!serverVersion.equals(clientVersion)) {
        WidgetFactory.showAlert(stage, "Incompatible Version", "The VPin Server you are connecting to has version " + serverVersion + ".", "Please start the updater.");
      }

      stage.close();
      Studio.loadStudio(WidgetFactory.createStage(), client);
    }
  }

  public void setStage(Stage stage) {
    this.stage = stage;
    this.onConnectionRefresh();
  }

  private void installServer() {
    try {
      ServerInstallationUtil.install();
      Updater.restartServer();
      main.getTop().setVisible(false);

      FXMLLoader loader = new FXMLLoader(LoadingOverlayController.class.getResource("loading-overlay.fxml"));
      BorderPane loadingOverlay = loader.load();
      LoadingOverlayController ctrl = loader.getController();
      ctrl.setLoadingMessage("Installing Server, waiting for initial connect...");

      main.setCenter(loadingOverlay);

      new Thread(() -> {
        while (client.getSystemService().getVersion() == null) {
          try {
            LOG.info("Waiting for server...");
            Thread.sleep(2000);
          } catch (InterruptedException e) {
            LOG.error("server wait error");
          }
        }

        LOG.info("Found server startup, running on version " + client.getSystemService().getVersion() + ", starting table scan.");
        Platform.runLater(() -> {
          stage.close();
          ProgressDialog.createProgressDialog(new ServiceInstallationProgressModel(Studio.client));
          Studio.loadStudio(WidgetFactory.createStage(), client);
        });
      }).start();
    } catch (Exception e) {
      LOG.error("Server installation failed: " + e.getMessage(), e);
      WidgetFactory.showAlert(stage, "Server installation failed.", "Error: " + e.getMessage());
    }
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    tableView.setPlaceholder(new Label("                 No connections found.\n" +
        "Install the service or connect to another system."));

    this.installBtn.setVisible(ServerInstallationUtil.SERVER_EXE.exists());
    this.installBtn.setDisable(client.getSystemService().getVersion() != null);
    this.helpBtn.setVisible(ServerInstallationUtil.SERVER_EXE.exists());

    connectBtn.setDisable(true);
    tableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> connectBtn.setDisable(newValue == null));

    Font font = Font.font("Impact", FontPosture.findByName("regular"), 28);
    studioLabel.setFont(font);
    versionLabel.setText(Studio.getVersion());

    List<VPinConnection> connections = new ArrayList<>();
    data = FXCollections.observableList(connections);
    tableView.setItems(data);

    nameColumn.setCellValueFactory(cellData -> {
      VPinConnection value = cellData.getValue();
      return new SimpleObjectProperty(value.getName());
    });

    hostColumn.setCellValueFactory(cellData -> {
      VPinConnection value = cellData.getValue();
      return new SimpleObjectProperty(value.getHost());
    });

    avatarColumn.setCellValueFactory(cellData -> {
      VPinConnection value = cellData.getValue();
      ImageView view = new ImageView(value.getAvatar());
      view.setPreserveRatio(true);
      view.setFitWidth(50);
      view.setFitHeight(50);
      CommonImageUtil.setClippedImage(view, (int) (value.getAvatar().getWidth() / 2));
      return new SimpleObjectProperty(view);
    });

    actionColumn.setCellValueFactory(cellData -> {
      VPinConnection value = cellData.getValue();
      if (value.getHost().equals("localhost")) {
        return new SimpleObjectProperty<>("");
      }

      Button button = new Button();
      button.setStyle("-fx-border-radius: 6px;");
      FontIcon icon = new FontIcon("mdi2d-delete-outline");
      icon.setIconSize(8);
      icon.setIconColor(Paint.valueOf("#FFFFFF"));
      button.setGraphic(icon);
      button.setOnAction(event -> {
        Optional<ButtonType> result = WidgetFactory.showConfirmation(stage, "Delete connection to '" + value + "'?");
        if (result.isPresent() && result.get().equals(ButtonType.OK)) {
          store.removeValue(String.valueOf(value.getHost()));
          onConnectionRefresh();
        }
      });
      return new SimpleObjectProperty(button);
    });

    tableView.setRowFactory(tv -> {
      TableRow<VPinConnection> row = new TableRow<>();
      row.setOnMouseClicked(event -> {
        if (event.getClickCount() == 2 && (!row.isEmpty())) {
          onConnect();
        }
      });
      return row;
    });

    File propertiesFile = new File("config/connection.properties");
    propertiesFile.getParentFile().mkdirs();
    store = PropertiesStore.create(propertiesFile);
  }

  private VPinConnection checkConnection(String host) {
    ExecutorService executor = Executors.newSingleThreadExecutor();
    Future<VPinConnection> future = executor.submit(() -> {
      VPinStudioClient client = new VPinStudioClient(host);
      String version = client.getSystemService().getVersion();
      if (version != null) {
        VPinConnection connection = new VPinConnection();
        PreferenceEntryRepresentation avatarEntry = client.getPreference(PreferenceNames.AVATAR);
        PreferenceEntryRepresentation systemName = client.getPreference(PreferenceNames.SYSTEM_NAME);

        String name = systemName.getValue();
        if (StringUtils.isEmpty(name)) {
          name = UIDefaults.VPIN_NAME;
        }
        connection.setHost(host);
        connection.setName(name);

        if (!StringUtils.isEmpty(avatarEntry.getValue())) {
          connection.setAvatar(new Image(client.getAsset(AssetType.VPIN_AVATAR, avatarEntry.getValue())));
        }
        else {
          Image image = new Image(Studio.class.getResourceAsStream("avatar-default.png"));
          connection.setAvatar(image);
        }
        return connection;
      }
      return null;
    });

    executor.shutdownNow();

    try {
      return future.get(3000, TimeUnit.MILLISECONDS);
    } catch (Exception e) {
      //
    }

    return null;
  }
}
