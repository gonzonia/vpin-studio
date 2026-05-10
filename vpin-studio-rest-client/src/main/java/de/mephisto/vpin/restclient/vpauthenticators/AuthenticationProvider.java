package de.mephisto.vpin.restclient.vpauthenticators;

import com.fasterxml.jackson.annotation.JsonValue;

public enum AuthenticationProvider {
  VPU,
  VPF;

    @JsonValue
    public String toJsonValue() {
        return name();
    }

    public String toString() {
      return switch (this) {
          case VPF -> "VP Forum";
          case VPU -> "VP Universe";
      };
  }

  public String getUrl() {
      return switch (this) {
          case VPF -> "vpforums.org";
          case VPU -> "vpuniverse.com";
      };
  }
}
