package com.demo.token.service;

import com.demo.token.model.Token;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class JwtTokenBuilder {
    @Value("${jwt.secret.key}")
    private String secretKey;

    @Value("${jwt.issuer}")
    private String issuer;

    @Value("${jwt.token.expire}")
    private long tokenExpire;

    @Autowired
    private JwtService jwtService;

    public Token buildToken(String subject) {
        Token token = new Token();
        token.setToken(jwtService.createJWT(secretKey, UUID.randomUUID().toString(), issuer, subject, tokenExpire));
        token.setExpire(LocalDateTime.now().plusSeconds(tokenExpire));
        return token;
    }
}
