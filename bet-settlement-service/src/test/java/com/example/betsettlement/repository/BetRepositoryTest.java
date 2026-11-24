package com.example.betsettlement.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.betsettlement.model.BetStatus;
import com.example.betsettlement.repository.entity.BetEntity;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jdbc.test.autoconfigure.DataJdbcTest;

@DataJdbcTest
class BetRepositoryTest {
  @Autowired BetRepository betRepository;

  @Test
  void shouldFindPendingBetsByEventId() {
    // given
    var eventId = UUID.randomUUID();
    var pendingBet = betRepository.save(generateTestBet(eventId, BetStatus.PENDING));
    betRepository.save(generateTestBet(eventId, BetStatus.WON));
    betRepository.save(generateTestBet(UUID.randomUUID(), BetStatus.PENDING));

    // when
    var bets = betRepository.findByEventIdAndStatus(eventId, BetStatus.PENDING);

    // then
    assertThat(bets).containsExactly(pendingBet);
  }

  @Test
  void shouldSettlePendingBet() {
    // given
    var bet = betRepository.save(generateTestBet(UUID.randomUUID(), BetStatus.PENDING));

    // when
    var isSettled = betRepository.settlePendingBet(bet.getId(), BetStatus.WON);

    // then
    var settledBet = betRepository.findById(bet.getId()).orElseThrow();
    assertThat(isSettled).isTrue();
    assertThat(settledBet.getStatus()).isEqualTo(BetStatus.WON);
  }

  @Test
  void shouldNotSettleBetTwice() {
    // given
    var bet = betRepository.save(generateTestBet(UUID.randomUUID(), BetStatus.PENDING));
    betRepository.settlePendingBet(bet.getId(), BetStatus.WON);

    // when
    var isSettled = betRepository.settlePendingBet(bet.getId(), BetStatus.LOST);

    // then
    var settledBet = betRepository.findById(bet.getId()).orElseThrow();
    assertThat(isSettled).isFalse();
    assertThat(settledBet.getStatus()).isEqualTo(BetStatus.WON);
  }

  private BetEntity generateTestBet(UUID eventId, BetStatus status) {
    return BetEntity.builder()
        .userId(UUID.randomUUID())
        .eventId(eventId)
        .eventMarketId(UUID.randomUUID())
        .eventWinnerId(UUID.randomUUID())
        .betAmount(new BigDecimal("10.50"))
        .status(status)
        .build();
  }
}
