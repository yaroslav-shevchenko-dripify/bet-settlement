package com.example.betsettlement.messaging.kafka;

import static lombok.AccessLevel.PRIVATE;

import com.example.betsettlement.model.message.EventOutcomeMessage;
import com.example.betsettlement.service.BetSettlementService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = PRIVATE)
public class EventOutcomeConsumer {
  BetSettlementService betSettlementService;

  @KafkaListener(topics = "${event-outcomes.topic}")
  public void consume(EventOutcomeMessage eventOutcome) {
    log.info("Event outcome received: {}", eventOutcome);
    betSettlementService.requestSettlements(eventOutcome);
  }
}
