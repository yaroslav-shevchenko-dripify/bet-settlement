package com.example.betsettlement.controller;

import static lombok.AccessLevel.PRIVATE;

import com.example.betsettlement.model.dto.BetDto;
import com.example.betsettlement.service.BetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/bets")
@FieldDefaults(makeFinal = true, level = PRIVATE)
class BetController {
  BetService betService;

  @GetMapping
  @Operation(summary = "Find bets of an event")
  @ApiResponse(responseCode = "200", description = "Bets of the event, empty if there are none")
  @ApiResponse(responseCode = "400", description = "Missing or invalid event id")
  ResponseEntity<List<BetDto>> findBets(@RequestParam UUID eventId) {
    return ResponseEntity.ok(betService.findBetsByEventId(eventId));
  }
}
