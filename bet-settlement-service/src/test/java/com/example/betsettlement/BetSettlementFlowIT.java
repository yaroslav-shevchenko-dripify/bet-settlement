package com.example.betsettlement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.betsettlement.model.BetStatus;
import com.example.betsettlement.repository.BetRepository;
import com.example.betsettlement.repository.entity.BetEntity;
import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class BetSettlementFlowIT {
  @Autowired MockMvc mockMvc;
  @Autowired BetRepository betRepository;
  @Autowired KafkaTemplate<String, String> kafkaTemplate;

  @Test
  void shouldSettleAllBetsOfEvent() throws Exception {
    // given
    var eventId = UUID.randomUUID();
    var winnerId = UUID.randomUUID();
    var winningBet = betRepository.save(generateTestBet(eventId, winnerId));
    var losingBet = betRepository.save(generateTestBet(eventId, UUID.randomUUID()));
    var betOfOtherEvent = betRepository.save(generateTestBet(UUID.randomUUID(), winnerId));

    // when
    sendEventOutcome(generateTestOutcome(eventId, winnerId));

    // then
    await()
        .atMost(60, TimeUnit.SECONDS)
        .untilAsserted(() ->
            mockMvc
                .perform(get("/bets").param("eventId", eventId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath(statusOf(winningBet)).value("WON"))
                .andExpect(jsonPath(statusOf(losingBet)).value("LOST")));
    assertThat(getStatus(betOfOtherEvent)).isEqualTo(BetStatus.PENDING);
  }

  @Test
  void shouldSkipMalformedOutcome() throws Exception {
    // given
    var eventId = UUID.randomUUID();
    var winnerId = UUID.randomUUID();
    var bet = betRepository.save(generateTestBet(eventId, winnerId));

    // when
    sendEventOutcome("not json");
    sendEventOutcome(generateTestOutcome(eventId, winnerId));

    // then
    await()
        .atMost(60, TimeUnit.SECONDS)
        .untilAsserted(() -> assertThat(getStatus(bet)).isEqualTo(BetStatus.WON));
  }

  private void sendEventOutcome(String eventOutcome) throws Exception {
    kafkaTemplate.send("event-outcomes", "key", eventOutcome).get(10, TimeUnit.SECONDS);
  }

  private BetStatus getStatus(BetEntity bet) {
    return betRepository.findById(bet.getId()).orElseThrow().getStatus();
  }

  private String statusOf(BetEntity bet) {
    return "$[?(@.id == '%s')].status".formatted(bet.getId());
  }

  private String generateTestOutcome(UUID eventId, UUID winnerId) {
    return """
    {"eventId": "%s", "eventName": "Avengers vs X-Men", "eventWinnerId": "%s"}
    """
        .formatted(eventId, winnerId);
  }

  private BetEntity generateTestBet(UUID eventId, UUID eventWinnerId) {
    return BetEntity.builder()
        .userId(UUID.randomUUID())
        .eventId(eventId)
        .eventMarketId(UUID.randomUUID())
        .eventWinnerId(eventWinnerId)
        .betAmount(new BigDecimal("10.00"))
        .status(BetStatus.PENDING)
        .build();
  }
}
