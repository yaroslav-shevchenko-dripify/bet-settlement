package com.example.eventoutcome;

import com.example.eventoutcome.config.EventOutcomeProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(EventOutcomeProperties.class)
public class EventOutcomeApplication {

  public static void main(String[] args) {
    SpringApplication.run(EventOutcomeApplication.class, args);
  }
}
