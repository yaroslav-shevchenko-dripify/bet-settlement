package com.example.eventoutcome.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("event-outcomes")
public record EventOutcomeProperties(String topic, Duration sendTimeout) {}
