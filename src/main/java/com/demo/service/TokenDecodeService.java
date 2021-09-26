package com.demo.service;

import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

@Service
public class TokenDecodeService {
    @Value("${jwt.secret.key}")
    private String secretKey;

    @Autowired
    private JwtService jwtService;

    public Optional<Claims> decode(String token) {
        try {
            Claims claims = jwtService.decodeJWT(secretKey, token);

            LocalDateTime expiration = claims.getExpiration().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();

            return expiration.isAfter(LocalDateTime.now()) ? Optional.of(claims) : Optional.empty();
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
