package de.mephisto.vpin.ui.tables.vps;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.ui.vps.VpsUtil;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Paint;
import org.apache.commons.lang3.StringUtils;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.net.URI;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class VpsEntry extends HBox {
  private final static Logger LOG = LoggerFactory.getLogger(VpsEntry.class);

  public VpsEntry(String version, List<String> authors, String link, long changeDate, String update) {
    this.setAlignment(Pos.BASELINE_LEFT);
    this.setStyle("-fx-padding: 3px 0 0 0;");
    Label versionLabel = WidgetFactory.createDefaultLabel(version);
    versionLabel.setStyle("-fx-padding: 0 0 0 3px;-fx-font-size: 14px;");
    versionLabel.setPrefWidth(100);
    if (!StringUtils.isEmpty(version)) {
      versionLabel.setTooltip(new Tooltip(version));
    }
    this.getChildren().add(versionLabel);

    Label authorLabel = WidgetFactory.createDefaultLabel("");
    if (authors != null && !authors.isEmpty()) {
      authorLabel.setText(String.join(", ", authors));
      authorLabel.setTooltip(new Tooltip(String.join(", ", authors)));
    }


    authorLabel.setPrefWidth(266);
    this.getChildren().add(authorLabel);

    String abb = VpsUtil.abbreviate(link);
    String color = VpsUtil.getColor(abb);
    Button button = new Button(abb);
    button.getStyleClass().add("vps-button");
    button.setStyle("-fx-background-color: " + color + ";");
    button.setPrefWidth(70);
    button.setTooltip(new Tooltip(link));
    button.setOnAction(event -> {
      Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
      if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
        try {
          desktop.browse(new URI(link));
        } catch (Exception e) {
          LOG.error("Failed to open link: " + e.getMessage());
        }
      }
    });

    FontIcon fontIcon = new FontIcon();
    fontIcon.setIconSize(14);
    fontIcon.setIconColor(Paint.valueOf("#FFFFFF"));
    fontIcon.setIconLiteral(VpsUtil.getIconClass(abb));
    button.setGraphic(fontIcon);

    Label label = new Label();
    label.setPrefWidth(20);
    List<Node> children = new ArrayList<>();
    if (update != null) {
      FontIcon updateIcon = WidgetFactory.createUpdateIcon();
      label.setGraphic(updateIcon);
      label.setTooltip(new Tooltip("Update Available\n\n" + update));
    }
    children.add(label);

    if (abb.equals("Dropbox")) {
      children.add(button);
    }
    else if (abb.equals("Mega")) {
      children.add(spacer(5));
      button.setPrefWidth(60);
      children.add(button);
      children.add(spacer(5));
    }
    else {
      children.add(spacer(10));
      button.setPrefWidth(50);
      children.add(button);
      children.add(spacer(10));
    }

    this.getChildren().addAll(children);


    Label changedLabel = WidgetFactory.createDefaultLabel(DateFormat.getDateInstance().format(new Date(changeDate)));
    changedLabel.setPrefWidth(100);
    changedLabel.setStyle("-fx-padding: 0 3px 0 0;-fx-font-size: 14px;");
    changedLabel.setAlignment(Pos.BASELINE_RIGHT);
    changedLabel.setContentDisplay(ContentDisplay.RIGHT);
    if (changeDate == 0) {
      changedLabel.setText("");
    }
    this.getChildren().add(changedLabel);
  }


  public static Label spacer(int width) {
    Label spacer = new Label("");
    spacer.setPrefWidth(width);
    return spacer;
  }
}
