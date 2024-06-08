package de.mephisto.vpin.server.util;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.win32.StdCallLibrary;
import de.mephisto.vpin.server.VPinStudioServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class SystemUtil {
  private final static Logger LOG = LoggerFactory.getLogger(SystemUtil.class);

  public static String getVersion() {
    try {
      final Properties properties = new Properties();
      InputStream resourceAsStream = VPinStudioServer.class.getClassLoader().getResourceAsStream("version.properties");
      properties.load(resourceAsStream);
      resourceAsStream.close();
      return properties.getProperty("vpin.studio.version");
    } catch (IOException e) {
      LOG.error("Failed to read version number: " + e.getMessage(), e);
    }
    return null;
  }

  interface User32 extends StdCallLibrary {
    User32 INSTANCE = Native.loadLibrary("user32", User32.class);

    interface WNDENUMPROC extends StdCallCallback {
      boolean callback(Pointer hWnd, Pointer arg);
    }

    boolean EnumWindows(WNDENUMPROC lpEnumFunc, Pointer userData);

    int GetWindowTextA(Pointer hWnd, byte[] lpString, int nMaxCount);

    Pointer GetWindow(Pointer hWnd, int uCmd);
  }

  public static List<String> getAllWindowNames() {
    final List<String> windowNames = new ArrayList<>();
    final User32 user32 = User32.INSTANCE;
    user32.EnumWindows(new User32.WNDENUMPROC() {

      @Override
      public boolean callback(Pointer hWnd, Pointer arg) {
        byte[] windowText = new byte[512];
        user32.GetWindowTextA(hWnd, windowText, 512);
        String wText = Native.toString(windowText).trim();
        if (!wText.isEmpty()) {
          windowNames.add(wText);
        }
        return true;
      }
    }, null);

    return windowNames;
  }
}
