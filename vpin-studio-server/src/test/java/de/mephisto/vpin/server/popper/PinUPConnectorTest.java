package de.mephisto.vpin.server.popper;

import de.mephisto.vpin.restclient.popper.EmulatorNames;
import de.mephisto.vpin.restclient.popper.PinUPPlayerDisplay;
import de.mephisto.vpin.restclient.popper.TableDetails;
import de.mephisto.vpin.server.AbstractVPinServerTest;
import de.mephisto.vpin.server.games.Game;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class PinUPConnectorTest extends AbstractVPinServerTest {

  @Autowired
  private PinUPConnector connector;


  @Test
  public void testPupPlayerDisplays() {
    List<PinUPPlayerDisplay> pupPlayerDisplays = connector.getPupPlayerDisplays();
    assertFalse(pupPlayerDisplays.isEmpty());
  }

  @Test
  public void testGameAdding() {
    int l = connector.importGame(EM_TABLE, 1);
    if (l > 0) {
      assertTrue(connector.deleteGame(l));
    }
    assertTrue(l > 0);
  }

  @Test
  public void testTableInfo() {
    Game gameByFilename = connector.getGameByFilename(EM_TABLE_NAME);
    TableDetails tableDetails = connector.getTableDetails(gameByFilename.getId());
    String uuid = UUID.randomUUID().toString();
    tableDetails.setAuthor(uuid);
    connector.saveTableDetails(gameByFilename.getId(), tableDetails);
    TableDetails tableDetailsUpdated = connector.getTableDetails(gameByFilename.getId());
    assertEquals(tableDetailsUpdated.getAuthor(), uuid);
  }

  @Test
  public void testConnector() {
    List<Game> games = connector.getGames();
    assertFalse(games.isEmpty());

    List<Integer> gameIds = connector.getGameIds();
    assertFalse(gameIds.isEmpty());

    Game game = connector.getGame(games.get(0).getId());
    assertNotNull(game);

    Game gameByFilename = connector.getGameByFilename(game.getGameFile().getName());
    assertNotNull(gameByFilename);

    assertNotEquals(connector.getGameCount(), 0);

    assertNotNull(connector.getStartupScript());
    assertNotNull(connector.getEmulatorExitScript(EmulatorNames.VISUAL_PINBALL_X));
    assertNotNull(connector.getEmulatorExitScript(EmulatorNames.FUTURE_PINBALL));
    assertNotNull(connector.getEmulatorStartupScript(EmulatorNames.VISUAL_PINBALL_X));
    assertNotNull(connector.getEmulatorStartupScript(EmulatorNames.FUTURE_PINBALL));
  }

}
