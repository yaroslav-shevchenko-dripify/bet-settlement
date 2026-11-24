package com.example.betsettlement.model.message;

import static java.util.Objects.requireNonNull;

import com.example.betsettlement.model.BetStatus;
import java.util.UUID;

public record BetSettlementMessage(UUID betId, BetStatus status) {
  public BetSettlementMessage {
    requireNonNull(betId, "Bet id is required");
    requireNonNull(status, "Status is required");
  }
}
