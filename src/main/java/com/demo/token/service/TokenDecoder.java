package com.demo.token.service;

import com.demo.keycloak.exception.AuthenticationException;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;

import static com.demo.ErrorMessage.KEYCLOAK_TOKEN_ERROR_MESSAGE;

@Service
public class TokenDecoder {
    @Value("${jwt.secret.key}")
    private String secretKey;

    @Autowired
    private JwtService jwtService;

    public String getSubject(String token) {
        try {
            Claims claims = jwtService.decodeJWT(secretKey, token);

            LocalDateTime expiration = claims.getExpiration().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();

            if (expiration.isBefore(LocalDateTime.now())) {
                throw new AuthenticationException(KEYCLOAK_TOKEN_ERROR_MESSAGE);
            }

            return claims.getSubject();
        } catch (Exception e) {
            throw new AuthenticationException(KEYCLOAK_TOKEN_ERROR_MESSAGE);
        }
    }
}
