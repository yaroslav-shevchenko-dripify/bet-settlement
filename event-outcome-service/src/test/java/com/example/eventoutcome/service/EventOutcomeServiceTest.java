package com.example.eventoutcome.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doReturn;

import com.example.eventoutcome.config.EventOutcomeProperties;
import com.example.eventoutcome.exception.EventOutcomePublishingException;
import com.example.eventoutcome.model.dto.EventOutcomeDto;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ContextConfiguration(classes = EventOutcomeService.class)
@EnableConfigurationProperties(EventOutcomeProperties.class)
class EventOutcomeServiceTest {
  @MockitoBean KafkaTemplate<String, EventOutcomeDto> kafkaTemplate;
  @Autowired EventOutcomeService eventOutcomeService;

  @Test
  void shouldFailWhenKafkaIsNotAvailable() {
    // given
    var eventOutcome =
        new EventOutcomeDto(UUID.randomUUID(), "Avengers vs X-Men", UUID.randomUUID());
    doReturn(CompletableFuture.failedFuture(new KafkaException("Not available")))
        .when(kafkaTemplate)
        .send("event-outcomes", eventOutcome.eventId().toString(), eventOutcome);

    // then
    assertThatThrownBy(() -> eventOutcomeService.publishEventOutcome(eventOutcome))
        .isInstanceOf(EventOutcomePublishingException.class);
  }
}
