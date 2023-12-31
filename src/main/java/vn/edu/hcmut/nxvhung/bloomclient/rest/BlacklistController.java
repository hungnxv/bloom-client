package vn.edu.hcmut.nxvhung.bloomclient.rest;


import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.hcmut.nxvhung.bloomclient.dto.BlacklistDto;
import vn.edu.hcmut.nxvhung.bloomclient.service.BlacklistService;

@RestController
@RequestMapping(value = "/api/v1/blacklist")
@RequiredArgsConstructor
@Slf4j
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

  @GetMapping("/sendUpdatedBlacklist")
  public String sendBlacklist() {
    blacklistService.sendUpdatedBlacklist();
    return "Blacklists are being sent to Bloom server";
  }

  @GetMapping("/mayExist")
  public boolean mayExist(@RequestParam("phone") String phone)  {
    return blacklistService.mayExist(phone);
  }

  @GetMapping("/test")
  public boolean kaka()  {
     blacklistService.test();
     return true;
  }

  @PostMapping("/upload")
  public String upload(@RequestParam("file") MultipartFile multipartFile) throws IOException {
    blacklistService.uploadFile(multipartFile);
    return "Blacklist has been uploaded";
  }

}
