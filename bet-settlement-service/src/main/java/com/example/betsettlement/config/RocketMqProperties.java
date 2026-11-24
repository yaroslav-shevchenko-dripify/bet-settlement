package com.example.betsettlement.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("rocketmq")
public record RocketMqProperties(
    String nameServer,
    String topic,
    String producerGroup,
    String consumerGroup,
    Duration sendTimeout,
    int maxReconsumeTimes) {}
