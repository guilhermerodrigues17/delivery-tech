package com.deliverytech.delivery_api.service.impl;

import com.deliverytech.delivery_api.dto.request.LoginRequestDto;
import com.deliverytech.delivery_api.dto.request.RegisterUserRequestDto;
import com.deliverytech.delivery_api.dto.response.LoginResponseDto;
import com.deliverytech.delivery_api.dto.response.RegisterResponseDto;
import com.deliverytech.delivery_api.exceptions.BusinessException;
import com.deliverytech.delivery_api.exceptions.ConflictException;
import com.deliverytech.delivery_api.exceptions.ResourceNotFoundException;
import com.deliverytech.delivery_api.model.User;
import com.deliverytech.delivery_api.model.enums.Role;
import com.deliverytech.delivery_api.repository.UserRepository;
import com.deliverytech.delivery_api.security.TokenService;
import com.deliverytech.delivery_api.service.RestaurantService;
import com.deliverytech.delivery_api.service.UserService;
import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RestaurantService restaurantService;
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;

    @Timed("delivery_api.users.register.timer")
    public RegisterResponseDto createUser(RegisterUserRequestDto dto) {
        if (userRepository.existsByEmail(dto.getEmail())) throw new ConflictException("E-mail já utilizado");

        User userEntity = new User();
        userEntity.setName(dto.getName());
        userEntity.setEmail(dto.getEmail());
        userEntity.setPassword(passwordEncoder.encode(dto.getPassword()));
        userEntity.setRole(dto.getRole());
        userEntity.setActive(true);

        if(dto.getRole().equals(Role.RESTAURANT)) {
            if (dto.getRestaurantId() == null) {
                throw new BusinessException("Para usuários com a role 'RESTAURANT' o ID do restaurante é obrigatório");
            }
            var restaurant = restaurantService.findById(dto.getRestaurantId());
            userEntity.setRestaurant(restaurant);
        }

        var created = userRepository.save(userEntity);
        return new RegisterResponseDto(created.getId(), created.getName(), created.getEmail(), created.getRole());
    }

    @Override
    public User findById(String id) {
        return userRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new ResourceNotFoundException("Recurso não encontrado"));
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Recurso não encontrado"));
    }

    @Override
    @Timed("delivery_api.users.login.timer")
    public LoginResponseDto login(LoginRequestDto dto) {
        var userAndPass = new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getPassword());
        Authentication authentication = authenticationManager.authenticate(userAndPass);
        var user = (User) authentication.getPrincipal();

        String token = tokenService.generateToken(user);
        return new LoginResponseDto(token);
    }
}
