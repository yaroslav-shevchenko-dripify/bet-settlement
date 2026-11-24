package com.example.betsettlement.service.mapper;

import com.example.betsettlement.model.dto.BetDto;
import com.example.betsettlement.repository.entity.BetEntity;
import java.util.List;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BetMapper {
  BetDto toDto(BetEntity entity);

  List<BetDto> toListDto(List<BetEntity> entities);
}
