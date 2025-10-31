package com.deliverytech.delivery_api.controller;

import com.deliverytech.delivery_api.dto.request.LoginRequestDto;
import com.deliverytech.delivery_api.dto.request.RegisterUserRequestDto;
import com.deliverytech.delivery_api.dto.response.LoginResponseDto;
import com.deliverytech.delivery_api.dto.response.RegisterResponseDto;
import com.deliverytech.delivery_api.dto.response.wrappers.ApiResponseWrapper;
import com.deliverytech.delivery_api.service.impl.UserServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserServiceImpl userService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto dto) {
        LoginResponseDto response = userService.login(dto);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponseWrapper<RegisterResponseDto>> register(@Valid @RequestBody RegisterUserRequestDto dto) {
        var userCreated = userService.createUser(dto);
        var response = ApiResponseWrapper.of(userCreated);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
