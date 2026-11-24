package com.example.betsettlement.model.message;

import static java.util.Objects.requireNonNull;

import java.util.UUID;

public record EventOutcomeMessage(UUID eventId, String eventName, UUID eventWinnerId) {
  public EventOutcomeMessage {
    requireNonNull(eventId, "Event id is required");
    requireNonNull(eventName, "Event name is required");
    requireNonNull(eventWinnerId, "Event winner id is required");
  }
}
