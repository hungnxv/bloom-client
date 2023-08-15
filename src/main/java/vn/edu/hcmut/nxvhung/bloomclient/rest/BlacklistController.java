package vn.edu.hcmut.nxvhung.bloomclient.rest;


import java.io.IOException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.hcmut.nxvhung.bloomclient.service.BlacklistService;
import vn.edu.hcmut.nxvhung.bloomfilter.dto.Message;

@RestController
@RequestMapping(value = "/api/v1/blacklist")
public class BlacklistController {

  private final BlacklistService blacklistService;

  public BlacklistController(BlacklistService blacklistService) {
    this.blacklistService = blacklistService;
  }

  @PostMapping
  public boolean add(String phone) {
    blacklistService.add(phone, null);
    return true;
  }

  @DeleteMapping
  public boolean remove(String phone) {
    return blacklistService.remove(phone);
  }


  @GetMapping("/init")
  public String init() throws IOException {
    blacklistService.init();
    return "OK";
  }

}
