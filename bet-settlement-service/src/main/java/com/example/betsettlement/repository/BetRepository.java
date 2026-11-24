package com.example.betsettlement.repository;

import com.example.betsettlement.model.BetStatus;
import com.example.betsettlement.repository.entity.BetEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BetRepository extends CrudRepository<BetEntity, UUID> {

  List<BetEntity> findByEventId(UUID eventId);

  List<BetEntity> findByEventIdAndStatus(UUID eventId, BetStatus status);

  @Modifying
  @Query(
      """
      UPDATE "bets" SET status = :status
      WHERE id = :id AND status = 'PENDING'
      """)
  boolean settlePendingBet(@Param("id") UUID id, @Param("status") BetStatus status);
}
