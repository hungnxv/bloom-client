package vn.edu.hcmut.nxvhung.bloomclient.rest;


import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.hcmut.nxvhung.bloomclient.dto.BlacklistDto;
import vn.edu.hcmut.nxvhung.bloomclient.service.BlacklistService;

@RestController
@RequestMapping(value = "/api/v1/blacklist")
@RequiredArgsConstructor
public class BlacklistController {

  private final BlacklistService blacklistService;

  @PostMapping
  public boolean add(@RequestBody BlacklistDto blacklistDto) {
    blacklistService.add(blacklistDto.getPhone(), blacklistDto.getExpiredDate());
    return true;
  }

  @DeleteMapping
  public boolean remove(@RequestParam  String phone) {
    return blacklistService.remove(phone);
  }


  @GetMapping("/file/init")
  public String init() throws IOException {
    blacklistService.initFromFile();
    return "Blacklists are being imported";
  }

  @GetMapping("/db/init")
  public String initFromDb() {
    blacklistService.initFromDatabase();
    return "Blacklists are being imported";
  }

  @GetMapping("/mayExist")
  public boolean mayExist(@RequestParam("phone") String phone)  {
    return blacklistService.mayExist(phone);
  }


}
