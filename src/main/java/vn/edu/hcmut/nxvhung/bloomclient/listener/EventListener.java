package vn.edu.hcmut.nxvhung.bloomclient.listener;

import jakarta.jms.JMSException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import vn.edu.hcmut.nxvhung.bloomclient.service.BlacklistService;
import vn.edu.hcmut.nxvhung.bloomfilter.dto.Message;


@Component
@Slf4j
public class EventListener {

  private final BlacklistService blacklistService;

  public EventListener(BlacklistService blacklistService) {
    this.blacklistService = blacklistService;
  }

  @JmsListener(destination = "${queue.response}")
  public void receiveMessage(final Message message) throws JMSException {
    log.info("Received message {}", message.toString());
    blacklistService.handleBFSUpdate(message);
  }


}
