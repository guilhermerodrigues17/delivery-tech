package com.deliverytech.delivery_api.service;

import com.deliverytech.delivery_api.dto.request.RegisterUserRequestDto;

import com.deliverytech.delivery_api.model.User;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends UserDetailsService {

    User createUser(RegisterUserRequestDto dto);
    User findById(String id);
    User findByEmail(String email);
}
