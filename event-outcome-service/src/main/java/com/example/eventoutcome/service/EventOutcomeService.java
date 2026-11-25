package com.example.eventoutcome.service;

import static java.lang.String.format;
import static lombok.AccessLevel.PRIVATE;

import com.example.eventoutcome.config.EventOutcomeProperties;
import com.example.eventoutcome.exception.EventOutcomePublishingException;
import com.example.eventoutcome.model.dto.EventOutcomeDto;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = PRIVATE)
public class EventOutcomeService {
  KafkaTemplate<String, EventOutcomeDto> kafkaTemplate;
  EventOutcomeProperties properties;

  public void publishEventOutcome(EventOutcomeDto eventOutcome) {
    try {
      kafkaTemplate
          .send(properties.topic(), eventOutcome.eventId().toString(), eventOutcome)
          .get(properties.sendTimeout().toMillis(), TimeUnit.MILLISECONDS);
      log.info("Event outcome published: {}", eventOutcome);
    } catch (Exception e) {
      throw new EventOutcomePublishingException(
          format("Outcome of event with id %s was not published", eventOutcome.eventId()), e);
    }
  }
}
