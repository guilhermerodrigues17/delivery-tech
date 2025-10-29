package com.deliverytech.delivery_api.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.deliverytech.delivery_api.model.User;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class TokenService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    private Algorithm algorithm;

    private JWTVerifier verifier;

    @PostConstruct
    private void init() {
        this.algorithm = Algorithm.HMAC256(secret);
        this.verifier = JWT.require(algorithm).build();
    }

    public String generateToken(User user) {
        Instant now = Instant.now();
        Instant expiryDate = now.plusSeconds(expiration);

        return JWT.create()
            .withSubject(user.getEmail())
            .withIssuedAt(now)
            .withExpiresAt(expiryDate)
            .withClaim("userId", user.getId().toString())
            .withClaim("role", user.getRole().name())
            .withClaim("restaurantId", user.getRestaurant() != null ? user.getRestaurant().getId().toString() : null)
            .sign(algorithm);
    }

    private DecodedJWT validateAndDecodeToken(String token) throws JWTVerificationException {
        return verifier.verify(token);
    }

    public String extractUsername(String token) {
        DecodedJWT decodedJWT = validateAndDecodeToken(token);
        return decodedJWT.getSubject();
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        String username = extractUsername(token);
        return username != null && username.equals(userDetails.getUsername());
    }

    public String extractClaimByName(String token, String claimName) {
        DecodedJWT decodedJWT = validateAndDecodeToken(token);
        return decodedJWT.getClaim(claimName).asString();
    }

    public Instant extractExpiration(String token) {
        DecodedJWT decodedJWT = validateAndDecodeToken(token);
        return decodedJWT.getExpiresAtAsInstant();
    }
}
