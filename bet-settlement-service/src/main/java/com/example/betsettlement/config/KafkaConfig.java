package com.example.betsettlement.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.ExponentialBackOff;

@Configuration
public class KafkaConfig {

  // retries until RocketMQ is available again, malformed messages are logged and skipped
  @Bean
  DefaultErrorHandler kafkaErrorHandler() {
    return new DefaultErrorHandler(new ExponentialBackOff());
  }
}
