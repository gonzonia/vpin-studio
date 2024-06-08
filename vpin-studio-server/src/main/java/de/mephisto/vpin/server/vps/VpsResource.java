package de.mephisto.vpin.server.vps;

import de.mephisto.vpin.connectors.vps.VpsDiffer;
import de.mephisto.vpin.connectors.vps.model.VpsTable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;

@RestController
@RequestMapping(API_SEGMENT + "vps")
public class VpsResource {

  @Autowired
  private VpsService vpsService;

  @GetMapping("/vpsTables")
  public List<VpsTable> getTables() {
    return vpsService.getTables();
  }

  @GetMapping("/vpsTable/{extTableId}")
  public VpsTable getTableById(@PathVariable("extTableId") String extTableId) {
    return vpsService.getTableById(extTableId);
  }

  @GetMapping("/find/{rom}/{term}")
  public List<VpsTable> find(@PathVariable("term") String term, @PathVariable("rom") String rom) {
    return vpsService.find(term, rom);
  }

  @GetMapping("/update")
  public boolean update() {
    List<VpsDiffer> diffs = vpsService.update();
    return !diffs.isEmpty();
  }

  @GetMapping("/reload")
  public boolean reload() {
    return vpsService.reload();
  }

  @GetMapping("/changeDate")
  public Date getChangeDate() {
    return vpsService.getChangeDate();
  }

}
