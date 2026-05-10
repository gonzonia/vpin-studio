package de.mephisto.vpin.restclient.validation;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ValidatorMedia {
  audio("Audio"),
  video("Video"),
  image("Image"),
  imageOrVideo("Image or Video");


  private final String displayName;

    @JsonValue
    public String toJsonValue() {
        return name();
    }

  ValidatorMedia(String displayName) {
    this.displayName = displayName;
  }

  public String getDisplayName() {
    return displayName;
  }

  @Override
  public String toString() {
    return getDisplayName();
  }
}
