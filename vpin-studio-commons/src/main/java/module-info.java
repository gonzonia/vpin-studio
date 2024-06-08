open module de.mephisto.vpin.commons {
  requires org.slf4j;
  requires org.apache.commons.lang3;
  requires org.apache.commons.io;
  requires org.kordamp.ikonli.javafx;
  requires com.github.spotbugs.annotations;
  requires java.desktop;
  requires imgscalr.lib;
  requires filters;

  requires javafx.controls;
  requires javafx.fxml;
  requires javafx.web;
  requires javafx.swing;

  requires org.controlsfx.controls;
  requires eu.hansolo.tilesfx;

  // add icon pack modules
  requires javafx.media;

  requires de.mephisto.vpin.restclient;
  requires jnativehook;
  requires java.logging;
  requires de.mephisto.vpin.connectors.vps;
  requires de.mephisto.vpin.connectors.iscored;
  requires sevenzipjbinding;
  requires org.apache.commons.configuration2;

  exports de.mephisto.vpin.commons;
  exports de.mephisto.vpin.commons.fx;
  exports de.mephisto.vpin.commons.fx.widgets;
  exports de.mephisto.vpin.commons.fx.discord;
  exports de.mephisto.vpin.commons.utils;
  exports de.mephisto.vpin.commons.utils.media;
}