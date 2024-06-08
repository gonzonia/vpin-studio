package de.mephisto.vpin.restclient.tournaments;

import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.client.VPinStudioClientService;
import de.mephisto.vpin.restclient.preferences.PreferencesServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*********************************************************************************************************************
 * Tournaments
 ********************************************************************************************************************/
public class TournamentsServiceClient extends VPinStudioClientService {
  private final static Logger LOG = LoggerFactory.getLogger(TournamentsServiceClient.class);
  private final PreferencesServiceClient preferencesServiceClient;

  public TournamentsServiceClient(VPinStudioClient client, PreferencesServiceClient preferencesServiceClient) {
    super(client);
    this.preferencesServiceClient = preferencesServiceClient;
  }

  public TournamentConfig getConfig() {
    return getRestClient().get(API + "tournaments/config", TournamentConfig.class);
  }

  public TournamentSettings getSettings() {
    return getRestClient().get(API + "tournaments", TournamentSettings.class);
  }

  public TournamentSettings saveSettings(TournamentSettings s) throws Exception {
    try {
      TournamentSettings post = getRestClient().post(API + "tournaments", s, TournamentSettings.class);
      preferencesServiceClient.notifyPreferenceChange(PreferenceNames.TOURNAMENTS_SETTINGS, post);
      return post;
    } catch (Exception e) {
      LOG.error("Failed to save tournament settings: " + e.getMessage(), e);
      throw e;
    }
  }

  public boolean synchronize() {
    return getRestClient().get(API + "tournaments/synchronize", Boolean.class);
  }

  public boolean synchronize(TournamentMetaData tournamentMetaData) throws Exception {
    try {
      return getRestClient().post(API + "tournaments/synchronize", tournamentMetaData, Boolean.class);
    } catch (Exception e) {
      LOG.error("Failed to save tournament meta data: " + e.getMessage(), e);
      throw e;
    }
  }
}
