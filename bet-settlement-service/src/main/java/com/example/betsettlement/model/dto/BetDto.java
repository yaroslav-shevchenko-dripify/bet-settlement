package com.example.betsettlement.model.dto;

import com.example.betsettlement.model.BetStatus;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.Builder;

@Builder
public record BetDto(
    UUID id,
    UUID userId,
    UUID eventId,
    UUID eventMarketId,
    UUID eventWinnerId,
    BigDecimal betAmount,
    BetStatus status) {}
