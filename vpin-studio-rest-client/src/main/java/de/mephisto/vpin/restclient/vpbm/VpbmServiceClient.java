package de.mephisto.vpin.restclient.vpbm;

import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.client.VPinStudioClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;

public class VpbmServiceClient extends VPinStudioClientService {
  private final static Logger LOG = LoggerFactory.getLogger(VpbmServiceClient.class);

  public VpbmServiceClient(VPinStudioClient client) {
    super(client);
  }

  public boolean isUpdateAvailable() {
    return getRestClient().get(API + "vpbm/updateavailable", Boolean.class);
  }

  public boolean update() {
    return getRestClient().get(API + "vpbm/update", Boolean.class);
  }

  public String getVersion() {
    final RestTemplate restTemplate = new RestTemplate();
    return restTemplate.getForObject(getRestClient().getBaseUrl() + API + "vpbm/version", String.class);
  }
}
