package vn.edu.hcmut.nxvhung.bloomclient.rest;

import vn.edu.hcmut.nxvhung.bloomfilter.dto.Message;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.hcmut.nxvhung.bloomclient.sender.BloomSender;
import vn.edu.hcmut.nxvhung.bloomfilter.hash.Hash;
import vn.edu.hcmut.nxvhung.bloomfilter.impl.Key;
import vn.edu.hcmut.nxvhung.bloomfilter.impl.MergeableCountingBloomFilter;

@RestController

public class TestController {

  private BloomSender bloomSender;


  public TestController(BloomSender bloomSender){
    this.bloomSender = bloomSender;
  }

  @GetMapping("/test")
  public String test() {
    MergeableCountingBloomFilter bloomFilter = new MergeableCountingBloomFilter(20, 3, Hash.MURMUR_HASH, 4);
    bloomFilter.add(Key.of("08232832832"));
    bloomFilter.add(Key.of("08232832822"));
    bloomFilter.add(Key.of("0772751097"));


    bloomSender.sendMessage("company_A_request", new Message(1, bloomFilter));
    return "OK";
  }

}
