package com.example.betsettlement.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("rocketmq")
public record RocketMqProperties(
    String nameServer, String topic, String producerGroup, String consumerGroup) {}
