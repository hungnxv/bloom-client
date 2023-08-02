package vn.edu.hcmut.nxvhung.bloomclient.sender;


import jakarta.jms.JMSException;
import vn.edu.hcmut.nxvhung.bloomfilter.common.dto.dto.Message;
import jakarta.jms.Session;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Component;

@Component
public class BloomSender {

  @Value("${spring.activemq.broker-url}")
  private String value;

  private final JmsTemplate jmsTemplate;

  public BloomSender(JmsTemplate jmsTemplate) {
    this.jmsTemplate = jmsTemplate;
  }

  public void sendMessage(String destination, final Message message) {
    jmsTemplate.convertAndSend(destination, message);
    };
  }


}
