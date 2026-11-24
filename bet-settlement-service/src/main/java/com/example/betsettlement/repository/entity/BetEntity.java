package com.example.betsettlement.repository.entity;

import static lombok.AccessLevel.PRIVATE;

import com.example.betsettlement.model.BetStatus;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("bets")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = PRIVATE)
public class BetEntity {
  @Id UUID id;
  UUID userId;
  UUID eventId;
  UUID eventMarketId;
  UUID eventWinnerId;
  BigDecimal betAmount;
  BetStatus status;

  public BetStatus resolveStatus(UUID actualWinnerId) {
    return eventWinnerId.equals(actualWinnerId) ? BetStatus.WON : BetStatus.LOST;
  }
}
