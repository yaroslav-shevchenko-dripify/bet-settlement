package com.example.eventoutcome.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import lombok.Builder;

@Builder
public record EventOutcomeDto(
    @NotNull(message = "Event id is required") UUID eventId,
    @NotBlank(message = "Event name is required")
        @Size(max = 255, message = "Event name must be at most 255 characters")
        String eventName,
    @NotNull(message = "Event winner id is required") UUID eventWinnerId) {}
