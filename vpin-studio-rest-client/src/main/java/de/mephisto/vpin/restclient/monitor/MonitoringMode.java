package de.mephisto.vpin.restclient.monitor;

import com.fasterxml.jackson.annotation.JsonValue;


public enum MonitoringMode {
  frontendScreens, monitors;

    @JsonValue
    public String toJsonValue() {
        return name();
    }

  @Override
  public String toString() {
      return switch (this) {
          case monitors -> "All Monitors";
          case frontendScreens -> "Frontend Screens";
      };
  }
}
