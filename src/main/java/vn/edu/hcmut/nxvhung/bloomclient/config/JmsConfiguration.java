package vn.edu.hcmut.nxvhung.bloomclient.config;

import java.util.List;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;

@Configuration
@EnableJms
@ComponentScan(basePackages = "vn.edu.hcmut.nxvhung.bloomclient")
public class JmsConfiguration {

  @Value("${spring.activemq.broker-url}")
  private String brokerUrl;
  @Value("${spring.activemq.user}")
  private String username;
  @Value("${spring.activemq.password}")

  private String password;
  @Bean
  public ActiveMQConnectionFactory connectionFactory() {
    ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory();
    connectionFactory.setBrokerURL(brokerUrl);
    connectionFactory.setUserName(username);
    connectionFactory.setPassword(password);
    connectionFactory.setTrustAllPackages(true);
    connectionFactory.setTrustedPackages(List.of("vn.edu.hcmut.nxvhung"));

    return connectionFactory;
  }

  @Bean
  public JmsTemplate jmsTemplate() {
    JmsTemplate template = new JmsTemplate();
    template.setConnectionFactory(connectionFactory());

    return template;
  }

  @Bean
  public DefaultJmsListenerContainerFactory jmsListenerContainerFactory() {
    DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
    factory.setConnectionFactory(connectionFactory());
    factory.setConcurrency("1-1");
    // true: using jms topic, false: using jms queue
    factory.setPubSubDomain(false);

    return factory;
  }
}



