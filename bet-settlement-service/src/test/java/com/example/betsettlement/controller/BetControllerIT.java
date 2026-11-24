package com.example.betsettlement.controller;

import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.betsettlement.model.BetStatus;
import com.example.betsettlement.model.dto.BetDto;
import com.example.betsettlement.service.BetService;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(BetController.class)
class BetControllerIT {
  @Autowired MockMvc mockMvc;
  @MockitoBean BetService betService;

  @Test
  void shouldFindBetsByEventId() throws Exception {
    // given
    var bet = generateTestBet();
    doReturn(List.of(bet)).when(betService).findBetsByEventId(bet.eventId());

    // then
    mockMvc
        .perform(get("/bets").param("eventId", bet.eventId().toString()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].id").value(bet.id().toString()))
        .andExpect(jsonPath("$[0].betAmount").value(10.50))
        .andExpect(jsonPath("$[0].status").value("PENDING"));
  }

  private BetDto generateTestBet() {
    return BetDto.builder()
        .id(UUID.fromString("1d7efc6e-5fde-491d-89ee-5782a9b96115"))
        .eventId(UUID.fromString("55e1ab4f-198d-4d3b-ba21-6514aa14922e"))
        .betAmount(new BigDecimal("10.50"))
        .status(BetStatus.PENDING)
        .build();
  }
}
