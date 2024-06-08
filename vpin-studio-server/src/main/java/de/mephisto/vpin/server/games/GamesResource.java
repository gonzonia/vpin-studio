package de.mephisto.vpin.server.games;

import de.mephisto.vpin.restclient.games.FilterSettings;
import de.mephisto.vpin.restclient.games.GameDetailsRepresentation;
import de.mephisto.vpin.restclient.games.GameScoreValidation;
import de.mephisto.vpin.restclient.games.descriptors.DeleteDescriptor;
import de.mephisto.vpin.restclient.highscores.HighscoreFiles;
import de.mephisto.vpin.restclient.validation.ValidationState;
import de.mephisto.vpin.server.competitions.ScoreSummary;
import de.mephisto.vpin.server.highscores.HighscoreMetadata;
import de.mephisto.vpin.server.highscores.ScoreList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequestMapping(API_SEGMENT + "games")
public class GamesResource {
  private final static Logger LOG = LoggerFactory.getLogger(GamesResource.class);

  @Autowired
  private GameService gameService;

  @Autowired
  private GameFilterService gameFilterService;

  @GetMapping
  public List<Game> getGames() {
    return gameService.getGames();
  }

  @PostMapping("/filter")
  public List<Integer> getGamesFiltered(@RequestBody FilterSettings filterSettings) {
    return gameFilterService.filterGames(gameService, filterSettings);
  }

  @GetMapping("/ids")
  public List<Integer> getGameIds() {
    return gameService.getGameIds();
  }

  @GetMapping("/reload")
  public boolean reload() {
    return gameService.reload();
  }

  @GetMapping("/unknowns")
  public List<Integer> getUnknownGameIds() {
    return gameService.getUnknownGames();
  }

  @GetMapping("/knowns/{emulatorId}")
  public List<Game> getKnownGames(@PathVariable("emulatorId") int emulatorId) {
    return gameService.getKnownGames(emulatorId);
  }

  @GetMapping("/recent/{count}")
  public ScoreSummary getRecentHighscores(@PathVariable("count") int count) {
    return gameService.getRecentHighscores(count);
  }

  @GetMapping("/recent/{count}/{gameId}")
  public ScoreSummary getRecentHighscoresForGame(@PathVariable("count") int count, @PathVariable("gameId") int gameId) {
    return gameService.getRecentHighscores(count, gameId);
  }

  @GetMapping("/{id}")
  public Game getGame(@PathVariable("id") int id) {
    Game game = gameService.getGame(id);
    if (game == null) {
      throw new ResponseStatusException(NOT_FOUND, "Not game found for id " + id);
    }
    return game;
  }

  @GetMapping("/details/{id}")
  public GameDetailsRepresentation getGameDetails(@PathVariable("id") int id) {
    return gameService.getGameDetails(id);
  }

  @GetMapping("/validations/{id}")
  public List<ValidationState> getAllValidations(@PathVariable("id") int id) {
    Game game = gameService.getGame(id);
    if (game == null) {
      return Collections.emptyList();
    }
    return gameService.validate(game);
  }

  @GetMapping("/scorevalidation/{id}")
  public GameScoreValidation getGameScoreValidation(@PathVariable("id") int id) {
    return gameService.getGameScoreValidation(id);
  }

  @GetMapping("/scores/{id}")
  public ScoreSummary getScores(@PathVariable("id") int id) {
    return gameService.getScores(id);
  }

  @GetMapping("/highscorefiles/{id}")
  public HighscoreFiles getHighscoreFiles(@PathVariable("id") int id) {
    return gameService.getHighscoreFiles(id);
  }

  @GetMapping("/scorehistory/{id}")
  public ScoreList getScoreHistory(@PathVariable("id") int id) {
    return gameService.getScoreHistory(id);
  }

  @GetMapping("/scan/{id}")
  public Game scanGame(@PathVariable("id") int pupId) {
    LOG.info("Client initiated game scan for " + pupId);
    return gameService.scanGame(pupId);
  }

  @GetMapping("/scanscore/{id}")
  public HighscoreMetadata scanGameScore(@PathVariable("id") int pupId) {
    return gameService.scanScore(pupId);
  }

  @PostMapping("/delete")
  public boolean delete(@RequestBody DeleteDescriptor descriptor) {
    return gameService.deleteGame(descriptor);
  }

  @DeleteMapping("/reset/{gameId}")
  public boolean reset(@PathVariable("gameId") int gameId) {
    return gameService.resetGame(gameId);
  }

  @PostMapping("/save")
  public Game save(@RequestBody Game game) throws Exception {
    return gameService.save(game);
  }
}
