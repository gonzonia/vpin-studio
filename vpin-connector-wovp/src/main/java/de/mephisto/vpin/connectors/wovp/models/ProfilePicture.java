package de.mephisto.vpin.connectors.wovp.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ProfilePicture {
  @JsonProperty("x-large")
  private ProfilePictureVariant xLarge;

  public ProfilePictureVariant getXLarge() {
    return xLarge;
  }

  public void setXLarge(ProfilePictureVariant xLarge) {
    this.xLarge = xLarge;
  }
}
