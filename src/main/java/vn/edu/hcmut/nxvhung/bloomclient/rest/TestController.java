package vn.edu.hcmut.nxvhung.bloomclient.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.hcmut.nxvhung.bloomclient.sender.BloomSender;

@RestController
public class TestController {
  public TestController(BloomSender bloomSender){
    this.bloomSender = bloomSender;
  }
  private BloomSender bloomSender;
  @GetMapping("/test")
  public String test() {
    bloomSender.sendMessage("company_A_request", "Message from bloomclient");
    return "OK";
  }

}
