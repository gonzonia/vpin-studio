package de.mephisto.vpin.restclient.vr;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.File;

public class VRFilesInfo {
  private File dmdDeviceIniFile;
  private File dmdDeviceIniVrFile;

  private File vPinballXIniFile;
  private File vPinballXIniVrFile;

  private String dmdDeviceIni;
  private String dmdDeviceIniVr;
  private String vPinballXIni;
  private String vPinballXIniVr;

  @JsonIgnore
  public File getDmdDeviceIniFile() {
    return dmdDeviceIniFile;
  }

  public void setDmdDeviceIniFile(File dmdDeviceIniFile) {
    this.dmdDeviceIniFile = dmdDeviceIniFile;
  }

  @JsonIgnore
  public File getDmdDeviceIniVrFile() {
    return dmdDeviceIniVrFile;
  }

  public void setDmdDeviceIniVrFile(File dmdDeviceIniVrFile) {
    this.dmdDeviceIniVrFile = dmdDeviceIniVrFile;
  }

  @JsonIgnore
  @JsonProperty("vPinballXIniFile")
  public File getvPinballXIniFile() {
    return vPinballXIniFile;
  }

    @JsonProperty("vPinballXIniFile")
  public void setvPinballXIniFile(File vPinballXIniFile) {
    this.vPinballXIniFile = vPinballXIniFile;
  }

  @JsonIgnore
  @JsonProperty("vPinballXIniVrFile")
  public File getvPinballXIniVrFile() {
    return vPinballXIniVrFile;
  }

    @JsonProperty("vPinballXIniVrFile")
  public void setvPinballXIniVrFile(File vPinballXIniVrFile) {
    this.vPinballXIniVrFile = vPinballXIniVrFile;
  }

  public String getDmdDeviceIni() {
    return dmdDeviceIni;
  }

  public void setDmdDeviceIni(String dmdDeviceIni) {
    this.dmdDeviceIni = dmdDeviceIni;
  }

  public String getDmdDeviceIniVr() {
    return dmdDeviceIniVr;
  }

  public void setDmdDeviceIniVr(String dmdDeviceIniVr) {
    this.dmdDeviceIniVr = dmdDeviceIniVr;
  }

    @JsonProperty("vPinballXIni")
  public String getvPinballXIni() {
    return vPinballXIni;
  }

    @JsonProperty("vPinballXIni")
  public void setvPinballXIni(String vPinballXIni) {
    this.vPinballXIni = vPinballXIni;
  }

    @JsonProperty("vPinballXIniVr")
  public String getvPinballXIniVr() {
    return vPinballXIniVr;
  }

    @JsonProperty("vPinballXIniVr")
  public void setvPinballXIniVr(String vPinballXIniVr) {
    this.vPinballXIniVr = vPinballXIniVr;
  }
}
