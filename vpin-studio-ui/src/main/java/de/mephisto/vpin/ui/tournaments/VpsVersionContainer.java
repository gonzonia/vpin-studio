package de.mephisto.vpin.ui.tournaments;

import de.mephisto.vpin.connectors.vps.VPS;
import de.mephisto.vpin.connectors.vps.model.VpsTableVersion;
import de.mephisto.vpin.ui.vps.VpsUtil;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.net.URI;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;

public class VpsVersionContainer extends VBox {
  private final static Logger LOG = LoggerFactory.getLogger(VpsVersionContainer.class);
  private final static int TITLE_WIDTH = 100;

  public VpsVersionContainer(VpsTableVersion item, String customStyle, boolean downloadAction) {
    super(3);
    setPadding(new Insets(3));
    setMinHeight(124);
    setMaxHeight(124);
    setMaxWidth(500);

    String comment = item.getComment();
    if (comment != null && !comment.trim().isEmpty()) {
      Label title = new Label(comment);
      title.setStyle("-fx-text-fill: #FFFFFF;-fx-font-size : 14px;-fx-font-weight : bold;" + customStyle);
      this.getChildren().add(title);
    }

    if (item.getAuthors() != null && item.getAuthors().size() > 0) {
      String authors = String.join(", ", item.getAuthors());
      Label title = new Label(authors);
      title.getStyleClass().add("default-text");
      title.setStyle(customStyle);
      if (comment == null || comment.trim().isEmpty()) {
        title.setStyle("-fx-text-fill: #FFFFFF;-fx-font-size : 14px;-fx-font-weight : bold;" + customStyle);
      }
      this.getChildren().add(title);
      if (comment == null || comment.trim().isEmpty()) {
        Label spacer = new Label();
        spacer.setStyle("-fx-font-size : 14px;");
        this.getChildren().add(spacer);
      }
    }

    HBox row = new HBox(6);
    Label titleLabel = new Label("Version:");
    titleLabel.setPrefWidth(TITLE_WIDTH);
    titleLabel.setStyle("-fx-text-fill: #FFFFFF;-fx-font-size : 12px;-fx-font-weight : bold;" + customStyle);
    Label valueLabel = new Label(item.getVersion());
    valueLabel.setStyle("-fx-text-fill: #FFFFFF;-fx-font-size : 12px;" + customStyle);
    row.getChildren().addAll(titleLabel, valueLabel);

    this.getChildren().add(row);

    row = new HBox(6);
    titleLabel = new Label("Updated At:");
    titleLabel.setPrefWidth(TITLE_WIDTH);
    titleLabel.setStyle("-fx-text-fill: #FFFFFF;-fx-font-size : 12px;-fx-font-weight : bold;" + customStyle);
    valueLabel = new Label(DateFormat.getDateInstance().format(new Date(item.getUpdatedAt())));
    valueLabel.setStyle("-fx-text-fill: #FFFFFF;-fx-font-size : 12px;" + customStyle);
    row.getChildren().addAll(titleLabel, valueLabel);
    this.getChildren().add(row);

    row = new HBox(6);
    Button button = new Button("Download Table");

    if (downloadAction) {
      button.setOnAction(event -> {
        Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
        if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
          try {
            desktop.browse(new URI(VPS.getVpsTableUrl(item.getId())));
          } catch (Exception e) {
            LOG.error("Failed to open link: " + e.getMessage());
          }
        }
      });

      FontIcon icon = new FontIcon("mdi2o-open-in-new");
      icon.setIconSize(8);
      icon.setIconColor(Paint.valueOf("#FFFFFF"));
      button.setGraphic(icon);
      button.setStyle("-fx-font-size: 12px;");
      button.getStyleClass().add("external-component");
      row.getChildren().add(button);
    }
    else {
      List<String> features = item.getFeatures();
      if (features != null) {
        for (String feature : features) {
          Label badge = new Label(feature);
          badge.getStyleClass().add("white-label");
          badge.setTooltip(new Tooltip(VpsUtil.getFeatureColorTooltip(feature)));
          badge.getStyleClass().add("vps-badge");
          badge.setStyle("-fx-background-color: " + VpsUtil.getFeatureColor(feature) + ";");
          row.getChildren().add(badge);
        }
      }
    }
    this.getChildren().add(row);

    row.setPadding(new Insets(3, 0, 6, 0));
  }
}
