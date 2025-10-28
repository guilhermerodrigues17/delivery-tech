package com.deliverytech.delivery_api.service.impl;

import com.deliverytech.delivery_api.dto.request.RegisterUserRequestDto;
import com.deliverytech.delivery_api.exceptions.ConflictException;
import com.deliverytech.delivery_api.exceptions.ResourceNotFoundException;
import com.deliverytech.delivery_api.model.User;
import com.deliverytech.delivery_api.model.enums.Role;
import com.deliverytech.delivery_api.repository.UserRepository;
import com.deliverytech.delivery_api.service.RestaurantService;
import com.deliverytech.delivery_api.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RestaurantService restaurantService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByEmail(username).orElseThrow(() -> new UsernameNotFoundException(username));
    }

    public User createUser(RegisterUserRequestDto dto) {
        if (userRepository.existsByEmail(dto.getEmail())) throw new ConflictException("E-mail já utilizado");

        User userEntity = new User();
        userEntity.setName(dto.getName());
        userEntity.setEmail(dto.getEmail());
        userEntity.setPassword(passwordEncoder.encode(dto.getPassword()));
        userEntity.setRole(dto.getRole());
        userEntity.setActive(true);

        if(dto.getRole().equals(Role.RESTAURANT)) {
            var restaurant = restaurantService.findById(dto.getRestaurantId());
            userEntity.setRestaurant(restaurant);
        }

        return userRepository.save(userEntity);
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
}
