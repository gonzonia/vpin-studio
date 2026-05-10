package de.mephisto.vpin.restclient.vr;

import com.fasterxml.jackson.annotation.JsonIgnore;

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
  public File getVPinballXIniFile() {
    return vPinballXIniFile;
  }

  public void setVPinballXIniFile(File vPinballXIniFile) {
    this.vPinballXIniFile = vPinballXIniFile;
  }

  @JsonIgnore
  public File getVPinballXIniVrFile() {
    return vPinballXIniVrFile;
  }

  public void setVPinballXIniVrFile(File vPinballXIniVrFile) {
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

  public String getVPinballXIni() {
    return vPinballXIni;
  }

  public void setVPinballXIni(String vPinballXIni) {
    this.vPinballXIni = vPinballXIni;
  }

  public String getVPinballXIniVr() {
    return vPinballXIniVr;
  }

  public void setVPinballXIniVr(String vPinballXIniVr) {
    this.vPinballXIniVr = vPinballXIniVr;
  }
}
