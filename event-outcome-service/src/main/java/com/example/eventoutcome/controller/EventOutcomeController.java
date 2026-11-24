package com.example.eventoutcome.controller;

import static lombok.AccessLevel.PRIVATE;

import com.example.eventoutcome.model.dto.EventOutcomeDto;
import com.example.eventoutcome.service.EventOutcomeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/event-outcomes")
@FieldDefaults(makeFinal = true, level = PRIVATE)
class EventOutcomeController {
  EventOutcomeService eventOutcomeService;

  @PostMapping
  @Operation(summary = "Publish a sports event outcome")
  @ApiResponse(responseCode = "202", description = "Event outcome accepted for settlement")
  @ApiResponse(responseCode = "400", description = "Invalid event outcome")
  @ApiResponse(responseCode = "503", description = "Event outcome could not be published")
  @ResponseStatus(HttpStatus.ACCEPTED)
  void publishEventOutcome(@RequestBody @Valid EventOutcomeDto eventOutcome) {
    eventOutcomeService.publishEventOutcome(eventOutcome);
  }
}
