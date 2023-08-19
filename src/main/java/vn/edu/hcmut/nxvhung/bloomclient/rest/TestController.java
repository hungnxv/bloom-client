package vn.edu.hcmut.nxvhung.bloomclient.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.hcmut.nxvhung.bloomclient.sender.BloomSender;
import vn.edu.hcmut.nxvhung.bloomclient.service.BlacklistService;

@RestController
public class TestController {

  @Value("${company.name}")
  private String companyName;
  private BloomSender bloomSender;

  public TestController(BloomSender bloomSender){
    this.bloomSender = bloomSender;
  }

  @Autowired
  private BlacklistService blacklistService;
  @GetMapping("/test")
  public String test()  {
//    MergeableCountingBloomFilter bloomFilter = new MergeableCountingBloomFilter(479253, 10, Hash.MURMUR_HASH, 4);

//    Message message = new Message(1, blacklistService.getBlacklist());
//    message.setCompanyName(companyName);
//    bloomSender.sendMessage(message);

    blacklistService.initFromDatabase();
    blacklistService.sendUpdatedBlacklist();
    return "OK";
  }

}
