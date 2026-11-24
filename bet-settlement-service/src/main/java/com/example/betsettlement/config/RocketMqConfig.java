package com.example.betsettlement.config;

import com.example.betsettlement.messaging.rocketmq.BetSettlementConsumer;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(RocketMqProperties.class)
public class RocketMqConfig {

  @Bean(initMethod = "start", destroyMethod = "shutdown")
  DefaultMQProducer rocketMqProducer(RocketMqProperties properties) {
    var producer = new DefaultMQProducer(properties.producerGroup());
    producer.setNamesrvAddr(properties.nameServer());
    producer.setSendMsgTimeout((int) properties.sendTimeout().toMillis());
    return producer;
  }

  @Bean(initMethod = "start", destroyMethod = "shutdown")
  DefaultMQPushConsumer rocketMqConsumer(
      RocketMqProperties properties, BetSettlementConsumer betSettlementConsumer)
      throws MQClientException {
    var consumer = new DefaultMQPushConsumer(properties.consumerGroup());
    consumer.setNamesrvAddr(properties.nameServer());
    consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
    consumer.setMaxReconsumeTimes(properties.maxReconsumeTimes());
    consumer.subscribe(properties.topic(), "*");
    consumer.registerMessageListener(betSettlementConsumer);
    return consumer;
  }
}
