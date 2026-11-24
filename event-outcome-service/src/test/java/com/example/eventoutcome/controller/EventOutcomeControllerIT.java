package com.example.eventoutcome.controller;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.eventoutcome.exception.EventOutcomePublishingException;
import com.example.eventoutcome.model.dto.EventOutcomeDto;
import com.example.eventoutcome.service.EventOutcomeService;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

@WebMvcTest(EventOutcomeController.class)
class EventOutcomeControllerIT {
  @Autowired MockMvc mockMvc;
  @Autowired JsonMapper jsonMapper;
  @MockitoBean EventOutcomeService eventOutcomeService;

  @Test
  void shouldAcceptEventOutcome() throws Exception {
    // given
    var eventOutcome = generateTestEventOutcome();

    // when
    mockMvc
        .perform(
            post("/event-outcomes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(eventOutcome)))
        .andExpect(status().isAccepted());

    // then
    verify(eventOutcomeService, times(1)).publishEventOutcome(eventOutcome);
  }

  @Test
  void shouldFailValidation() throws Exception {
    mockMvc
        .perform(
            post("/event-outcomes")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"eventName\": \" \"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.eventId").value("Event id is required"))
        .andExpect(jsonPath("$.eventName").value("Event name is required"))
        .andExpect(jsonPath("$.eventWinnerId").value("Event winner id is required"));
  }

  @Test
  void shouldReturnServiceUnavailableWhenPublishingFails() throws Exception {
    // given
    var eventOutcome = generateTestEventOutcome();
    doThrow(new EventOutcomePublishingException("Not published", new RuntimeException()))
        .when(eventOutcomeService)
        .publishEventOutcome(eventOutcome);

    // then
    mockMvc
        .perform(
            post("/event-outcomes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(eventOutcome)))
        .andExpect(status().isServiceUnavailable())
        .andExpect(jsonPath("$.error").value("Not published"));
  }

  private EventOutcomeDto generateTestEventOutcome() {
    return EventOutcomeDto.builder()
        .eventId(UUID.fromString("55e1ab4f-198d-4d3b-ba21-6514aa14922e"))
        .eventName("Avengers vs X-Men")
        .eventWinnerId(UUID.fromString("d397af36-53bf-4030-901e-7dda1a8d76e6"))
        .build();
  }
}
