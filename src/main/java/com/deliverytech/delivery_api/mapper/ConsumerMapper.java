package com.deliverytech.delivery_api.mapper;

import com.deliverytech.delivery_api.dto.request.ConsumerRequestDto;
import com.deliverytech.delivery_api.dto.response.ConsumerResponseDto;
import com.deliverytech.delivery_api.model.Consumer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ConsumerMapper {

    ConsumerResponseDto toDto(Consumer consumer);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "orders", ignore = true)
    Consumer toEntity(ConsumerRequestDto dto);
}
