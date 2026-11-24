package com.example.betsettlement.service;

import static lombok.AccessLevel.PRIVATE;

import com.example.betsettlement.model.dto.BetDto;
import com.example.betsettlement.repository.BetRepository;
import com.example.betsettlement.service.mapper.BetMapper;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = PRIVATE)
public class BetService {
  BetRepository betRepository;
  BetMapper betMapper;

  public List<BetDto> findBetsByEventId(UUID eventId) {
    return betMapper.toListDto(betRepository.findByEventId(eventId));
  }
}
