package de.mephisto.vpin.connectors.wovp.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ProfilePicture {
  @JsonProperty("x-large")
  private ProfilePictureVariant xLarge;

  @JsonProperty("xLarge")
  public ProfilePictureVariant getxLarge() {
    return xLarge;
  }

  @JsonProperty("xLarge")
  public void setxLarge(ProfilePictureVariant xLarge) {
    this.xLarge = xLarge;
  }
}
