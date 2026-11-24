package com.example.betsettlement;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration
class TestcontainersConfiguration {

  @Bean
  @ServiceConnection
  KafkaContainer kafkaContainer() {
    return new KafkaContainer(DockerImageName.parse("apache/kafka:4.4.0"));
  }

  @Bean
  RocketMqContainer rocketMqContainer() {
    return new RocketMqContainer();
  }

  @Bean
  DynamicPropertyRegistrar rocketMqPropertyRegistrar(RocketMqContainer rocketMqContainer) {
    return registry -> registry.add("rocketmq.name-server", rocketMqContainer::getNameServer);
  }
}
