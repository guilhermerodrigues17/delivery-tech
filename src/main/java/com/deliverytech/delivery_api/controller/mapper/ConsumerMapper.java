package com.deliverytech.delivery_api.controller.mapper;

import com.deliverytech.delivery_api.controller.dto.ConsumerRequestDto;
import com.deliverytech.delivery_api.controller.dto.ConsumerResponseDto;
import com.deliverytech.delivery_api.model.Consumer;
import org.mapstruct.Mapper;


@Mapper(componentModel = "spring")
public interface ConsumerMapper {

    ConsumerResponseDto toDto(Consumer consumer);

    Consumer toEntity(ConsumerRequestDto dto);
}
