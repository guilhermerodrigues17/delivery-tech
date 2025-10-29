package com.deliverytech.delivery_api.service;

import com.deliverytech.delivery_api.dto.request.LoginRequestDto;
import com.deliverytech.delivery_api.dto.request.RegisterUserRequestDto;
import com.deliverytech.delivery_api.dto.response.LoginResponseDto;
import com.deliverytech.delivery_api.dto.response.RegisterResponseDto;
import com.deliverytech.delivery_api.model.User;

public interface UserService {

    RegisterResponseDto createUser(RegisterUserRequestDto dto);

    User findById(String id);

    User findByEmail(String email);

    LoginResponseDto login(LoginRequestDto dto);
}
