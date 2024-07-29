package de.mephisto.vpin.ui.tournaments.view;

import de.mephisto.vpin.connectors.mania.model.TournamentSearchResultItem;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.StringUtils;

public class TournamentSearchText extends VBox {

  public TournamentSearchText(TournamentSearchResultItem item) {
    super(3);

    Label titleLabel = new Label(item.getDisplayName());
    titleLabel.getStyleClass().add("default-headline");
    getChildren().add(titleLabel);

    Label descriptionLabel = new Label("");
    if (!StringUtils.isEmpty(item.getDescription()) && !item.getDescription().equals("null")) {
      descriptionLabel.setText(item.getDescription());
    }
    descriptionLabel.setWrapText(true);
    descriptionLabel.getStyleClass().add("default-text");
    getChildren().add(descriptionLabel);

    this.setPrefHeight(70);
  }
}
