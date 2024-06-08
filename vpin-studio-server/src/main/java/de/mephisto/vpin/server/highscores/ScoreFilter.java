package de.mephisto.vpin.server.highscores;

import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.server.players.PlayerService;
import de.mephisto.vpin.server.preferences.PreferenceChangedListener;
import de.mephisto.vpin.server.preferences.PreferencesService;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class ScoreFilter implements InitializingBean, PreferenceChangedListener {
  private final static Logger LOG = LoggerFactory.getLogger(ScoreFilter.class);

  @Autowired
  private PreferencesService preferencesService;

  @Autowired
  private PlayerService playerService;

  private boolean highscoreFilterEnabled;

  public boolean isScoreFiltered(@NonNull Score score) {
    if (StringUtils.isEmpty(score.getPlayerInitials())) {
      LOG.info("Filtered highscore update \"" + score + "\": player initials are empty");
      return true;
    }
    if (playerService.getAdminPlayer() == null && score.getPlayerInitials().equalsIgnoreCase("???")) {
      LOG.info("Filtered highscore update \"" + score + "\": player initials are ???");
      return true;
    }

    if (highscoreFilterEnabled && !playerService.getBuildInPlayers().isEmpty()) {
      if (playerService.getPlayerForInitials(-1, score.getPlayerInitials()) == null) {
        LOG.info("Filtered highscore update \"" + score + "\": player initials '" + score.getPlayerInitials() + "' are not on the allow list");
        return true;
      }
    }
    return false;
  }

  @Override
  public void preferenceChanged(String propertyName, Object oldValue, Object newValue) {
    if (!StringUtils.isEmpty(propertyName)) {
      if (propertyName.equals(PreferenceNames.HIGHSCORE_FILTER_ENABLED)) {
        refreshScoreFilterSettings();
      }
    }
  }

  private void refreshScoreFilterSettings() {
    highscoreFilterEnabled = (boolean) preferencesService.getPreferenceValue(PreferenceNames.HIGHSCORE_FILTER_ENABLED, false);
    LOG.info("Highscore Filter Toggle: " + highscoreFilterEnabled);
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    preferencesService.addChangeListener(this);
    refreshScoreFilterSettings();
  }
}
