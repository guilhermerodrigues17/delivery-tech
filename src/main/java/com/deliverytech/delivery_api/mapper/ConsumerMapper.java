package com.deliverytech.delivery_api.mapper;

import com.deliverytech.delivery_api.dto.request.ConsumerRequestDto;
import com.deliverytech.delivery_api.dto.response.ConsumerResponseDto;
import com.deliverytech.delivery_api.model.Consumer;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ConsumerMapper {

    ConsumerResponseDto toDto(Consumer consumer);

    Consumer toEntity(ConsumerRequestDto dto);
}
