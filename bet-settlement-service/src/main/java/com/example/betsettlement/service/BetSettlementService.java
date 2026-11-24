package com.example.betsettlement.service;

import static lombok.AccessLevel.PRIVATE;

import com.example.betsettlement.messaging.rocketmq.BetSettlementProducer;
import com.example.betsettlement.model.BetStatus;
import com.example.betsettlement.model.message.BetSettlementMessage;
import com.example.betsettlement.model.message.EventOutcomeMessage;
import com.example.betsettlement.repository.BetRepository;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = PRIVATE)
public class BetSettlementService {
  BetRepository betRepository;
  BetSettlementProducer betSettlementProducer;

  public void requestSettlements(EventOutcomeMessage eventOutcome) {
    var pendingBets =
        betRepository.findByEventIdAndStatus(eventOutcome.eventId(), BetStatus.PENDING);
    log.info("Found {} bets to settle for event {}", pendingBets.size(), eventOutcome.eventId());

    pendingBets.forEach(bet ->
        betSettlementProducer.send(
            new BetSettlementMessage(
                bet.getId(), bet.resolveStatus(eventOutcome.eventWinnerId()))));
  }

  public void settleBet(BetSettlementMessage betSettlement) {
    if (betRepository.settlePendingBet(betSettlement.betId(), betSettlement.status())) {
      log.info("Bet {} settled as {}", betSettlement.betId(), betSettlement.status());
    } else {
      log.warn("Bet {} is not pending, skipping", betSettlement.betId());
    }
  }
}
