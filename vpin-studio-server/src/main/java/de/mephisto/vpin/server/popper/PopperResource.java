package de.mephisto.vpin.server.popper;

import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.games.GameStatusService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;

/**
 * Legacy URls:
 * "curl -X POST --data-urlencode \"info=\" http://localhost:" + HttpServer.PORT + "/service/popperLaunch";
 * "curl -X POST --data-urlencode \"table=[GAMEFULLNAME]\" http://localhost:" + HttpServer.PORT + "/service/gameLaunch";
 * "curl -X POST --data-urlencode \"table=[GAMEFULLNAME]\" http://localhost:" + HttpServer.PORT + "/service/gameExit";
 */
@RestController
@RequestMapping("/service") //do not add api version
public class PopperResource {
  private final static Logger LOG = LoggerFactory.getLogger(PopperResource.class);

  @Autowired
  private GameService gameService;

  @Autowired
  private PopperService popperService;

  @Autowired
  private GameStatusService gameStatusService;

  @PostMapping("/gameLaunch")
  public boolean gameLaunch(@RequestParam("table") String table) {
    LOG.info("Received popper game launch event for " + table.trim());
    Game game = resolveGame(table);
    if (game == null) {
      LOG.warn("No game found for name '" + table);
      return false;
    }

    if (gameStatusService.getStatus().getGameId() == game.getId()) {
      LOG.info("Skipped launch event, since the game has been marked as active already.");
      return false;
    }

    new Thread(() -> {
      Thread.currentThread().setName("Popper Game Launch Thread");
      popperService.notifyTableStatusChange(game, true, TableStatusChangedOrigin.ORIGIN_POPPER);
    }).start();
    return game != null;
  }

  @PostMapping("/gameExit")
  public boolean gameExit(@RequestParam("table") String table) {
    LOG.info("Received popper game exit event for " + table.trim());
    Game game = resolveGame(table);
    if (game == null) {
      LOG.warn("No game found for name '" + table);
      return false;
    }

    if (!gameStatusService.getStatus().isActive()) {
      LOG.info("Skipped exit event, since the no game is currently running.");
      return false;
    }

    new Thread(() -> {
      Thread.currentThread().setName("Popper Game Exit Thread");
      popperService.notifyTableStatusChange(game, false, TableStatusChangedOrigin.ORIGIN_POPPER);
    }).start();
    return game != null;
  }

  @PostMapping("/popperLaunch")
  public boolean popperLaunch() {
    popperService.notifyPopperLaunch();
    return true;
  }

  private Game resolveGame(String table) {
    File tableFile = new File(table.trim());
    Game game = gameService.getGameByFilename(tableFile.getName());
    if (game == null && tableFile.getParentFile() != null) {
      game = gameService.getGameByFilename(tableFile.getParentFile().getName() + "\\" + tableFile.getName());
    }
    LOG.info("PopperResource Game Event Handler resolved \"" + game + "\" for table name \"" + table + "\"");
    return game;
  }

}
