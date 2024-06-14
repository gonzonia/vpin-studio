package de.mephisto.vpin.restclient.frontend;

public class FrontendControl {
  public static String FUNCTION_SHOW_OTHER = "Show Other";
  public static String FUNCTION_SHOW_HELP = "Game Help";
  public static String FUNCTION_SHOW_FLYER = "Game Info/Flyer";

  public static String FUNCTION_GAME_NEXT = "Game Next";
  public static String FUNCTION_GAME_PRIOR = "Game Prior";
  public static String FUNCTION_GAME_START = "Game Start";
  public static String FUNCTION_EXIT = "Exit Emulators";

  private String description;
  private int ctrlKey;
  private int id;
  private boolean active;

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public int getCtrlKey() {
    return ctrlKey;
  }

  public void setCtrlKey(int ctrlKey) {
    this.ctrlKey = ctrlKey;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }
}
