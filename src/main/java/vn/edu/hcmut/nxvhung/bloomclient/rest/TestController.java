package vn.edu.hcmut.nxvhung.bloomclient.rest;

import org.springframework.beans.factory.annotation.Value;
import vn.edu.hcmut.nxvhung.bloomfilter.dto.Message;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.hcmut.nxvhung.bloomclient.sender.BloomSender;
import vn.edu.hcmut.nxvhung.bloomfilter.hash.Hash;
import vn.edu.hcmut.nxvhung.bloomfilter.impl.Key;
import vn.edu.hcmut.nxvhung.bloomfilter.impl.MergeableCountingBloomFilter;

@RestController

public class TestController {

  @Value("${company.name}")
  private String companyName;
  private BloomSender bloomSender;


  public TestController(BloomSender bloomSender){
    this.bloomSender = bloomSender;
  }


  @GetMapping("/test")
  public String test() {
    MergeableCountingBloomFilter bloomFilter = new MergeableCountingBloomFilter(479253, 10, Hash.MURMUR_HASH, 4);



    Message message = new Message(1, bloomFilter);
    message.setCompanyName(companyName);
    bloomSender.sendMessage(message);
    return "OK";
  }

}
