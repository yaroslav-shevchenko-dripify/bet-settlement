package com.example.eventoutcome;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.eventoutcome.model.dto.EventOutcomeDto;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.kafka.KafkaContainer;
import tools.jackson.databind.json.JsonMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class EventOutcomeFlowIT {
  @Autowired MockMvc mockMvc;
  @Autowired JsonMapper jsonMapper;
  @Autowired KafkaContainer kafkaContainer;

  @Test
  void shouldPublishEventOutcome() throws Exception {
    // given
    var eventOutcome =
        new EventOutcomeDto(UUID.randomUUID(), "Avengers vs X-Men", UUID.randomUUID());
    var expectedMessage = jsonMapper.writeValueAsString(eventOutcome);

    // when
    mockMvc
        .perform(
            post("/event-outcomes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(expectedMessage))
        .andExpect(status().isAccepted());

    // then
    try (var consumer = createConsumer()) {
      consumer.subscribe(List.of("event-outcomes"));
      await()
          .pollInSameThread()
          .atMost(15, TimeUnit.SECONDS)
          .untilAsserted(() ->
              assertThat(consumer.poll(Duration.ofSeconds(1)))
                  .anyMatch(message ->
                      eventOutcome.eventId().toString().equals(message.key())
                          && expectedMessage.equals(message.value())));
    }
  }

  private KafkaConsumer<String, String> createConsumer() {
    return new KafkaConsumer<>(
        Map.of(
            "bootstrap.servers", kafkaContainer.getBootstrapServers(),
            "group.id", "event-outcome-test",
            "auto.offset.reset", "earliest"),
        new StringDeserializer(),
        new StringDeserializer());
  }
}
