package vn.edu.hcmut.nxvhung.bloomclient.listener;

import jakarta.jms.JMSException;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import vn.edu.hcmut.nxvhung.bloomclient.service.BlacklistService;
import vn.edu.hcmut.nxvhung.bloomfilter.dto.Message;


@Component
public class EventListener {

  private final BlacklistService blacklistService;

  public EventListener(BlacklistService blacklistService) {
    this.blacklistService = blacklistService;
  }

  @JmsListener(destination = "company_A_response")
  public void receiveMessage(final Message message) throws JMSException {
    blacklistService.updateMergedBlacklist(message.getBlacklist());
  }


}
