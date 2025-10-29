package com.deliverytech.delivery_api.security;

import com.deliverytech.delivery_api.model.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class SecurityService {

    public Optional<User> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof User) {
            return Optional.of((User) authentication.getPrincipal());
        }

        return Optional.empty();
    }

    public Optional<UUID> getCurrentUserId() {
        return getCurrentUser().map(User::getId);
    }

    public Optional<UUID> getCurrentUserRestaurantId() {
        return getCurrentUser().map(u -> u.getRestaurant().getId());
    }
}
