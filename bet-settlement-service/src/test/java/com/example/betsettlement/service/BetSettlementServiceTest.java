package com.example.betsettlement.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.example.betsettlement.exception.BetSettlementPublishingException;
import com.example.betsettlement.messaging.rocketmq.BetSettlementProducer;
import com.example.betsettlement.model.BetStatus;
import com.example.betsettlement.model.message.BetSettlementMessage;
import com.example.betsettlement.model.message.EventOutcomeMessage;
import com.example.betsettlement.repository.BetRepository;
import com.example.betsettlement.repository.entity.BetEntity;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ContextConfiguration(classes = BetSettlementService.class)
class BetSettlementServiceTest {
  private static final UUID EVENT_ID = UUID.fromString("55e1ab4f-198d-4d3b-ba21-6514aa14922e");
  private static final UUID WINNER_ID = UUID.fromString("d397af36-53bf-4030-901e-7dda1a8d76e6");
  private static final UUID LOSER_ID = UUID.fromString("1d7efc6e-5fde-491d-89ee-5782a9b96115");

  @MockitoBean BetRepository betRepository;
  @MockitoBean BetSettlementProducer betSettlementProducer;
  @Autowired BetSettlementService betSettlementService;

  @Test
  void shouldRequestSettlementForPendingBets() {
    // given
    var firstBet = generateTestBet(WINNER_ID);
    var secondBet = generateTestBet(LOSER_ID);
    doReturn(List.of(firstBet, secondBet))
        .when(betRepository)
        .findByEventIdAndStatus(EVENT_ID, BetStatus.PENDING);

    // when
    betSettlementService.requestSettlements(generateTestOutcome());

    // then
    verify(betSettlementProducer, times(1))
        .send(new BetSettlementMessage(firstBet.getId(), BetStatus.WON));
    verify(betSettlementProducer, times(1))
        .send(new BetSettlementMessage(secondBet.getId(), BetStatus.LOST));
  }

  @Test
  void shouldFailWhenSendingFails() {
    // given
    var bet = generateTestBet(WINNER_ID);
    doReturn(List.of(bet)).when(betRepository).findByEventIdAndStatus(EVENT_ID, BetStatus.PENDING);
    doThrow(new BetSettlementPublishingException("Not sent"))
        .when(betSettlementProducer)
        .send(any(BetSettlementMessage.class));

    // then
    assertThatThrownBy(() -> betSettlementService.requestSettlements(generateTestOutcome()))
        .isInstanceOf(BetSettlementPublishingException.class);
  }

  @Test
  void shouldSettleBet() {
    // given
    var betSettlement = new BetSettlementMessage(UUID.randomUUID(), BetStatus.WON);

    // when
    betSettlementService.settleBet(betSettlement);

    // then
    verify(betRepository, times(1)).settlePendingBet(betSettlement.betId(), BetStatus.WON);
  }

  private EventOutcomeMessage generateTestOutcome() {
    return new EventOutcomeMessage(EVENT_ID, "Avengers vs X-Men", WINNER_ID);
  }

  private BetEntity generateTestBet(UUID eventWinnerId) {
    return BetEntity.builder()
        .id(UUID.randomUUID())
        .userId(UUID.randomUUID())
        .eventId(EVENT_ID)
        .eventMarketId(UUID.randomUUID())
        .eventWinnerId(eventWinnerId)
        .betAmount(new BigDecimal("10.50"))
        .status(BetStatus.PENDING)
        .build();
  }
}
