package vn.edu.hcmut.nxvhung.bloomclient.sender;


import jakarta.jms.JMSException;
import jakarta.jms.Message;
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

  public void sendMessage(String destination, final String message) {
    jmsTemplate.send(destination, new MessageCreator() {
      public Message createMessage(Session session) throws JMSException {

        return session.createTextMessage(message + value);
      }
    });
  }


}
