package vn.edu.hcmut.nxvhung.bloomclient.listener;

import jakarta.jms.JMSException;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import vn.edu.hcmut.nxvhung.bloomfilter.dto.Message;


@Component
public class EventListener {


  @JmsListener(destination = "company_A_response")
  public void receiveMessage(final Message message) {
    System.out.println(message.getBlacklist());
  }


}
