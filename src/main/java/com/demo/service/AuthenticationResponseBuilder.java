package com.demo.service;

import com.demo.model.AuthenticationResponse;
import com.demo.model.Token;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class AuthenticationResponseBuilder {
    @Value("${jwt.secret.key}")
    private String secretKey;

    @Value("${jwt.issuer}")
    private String issuer;

    @Value("${access.token.expire}")
    private long accessTokenExpire;

    @Value("${refresh.token.expire}")
    private long refreshTokenExpire;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UuidGenerator uuidGenerator;

    public AuthenticationResponse buildAuthenticationResponse(String subject) {
        Token accessToken = new Token();
        accessToken.setToken(jwtService.createJWT(secretKey, uuidGenerator.generateRandomUuid(), issuer, subject, accessTokenExpire));
        accessToken.setExpire(LocalDateTime.now().plusSeconds(accessTokenExpire));

        Token refreshToken = new Token();
        refreshToken.setToken(jwtService.createJWT(secretKey, uuidGenerator.generateRandomUuid(), issuer, subject, refreshTokenExpire));
        refreshToken.setExpire(LocalDateTime.now().plusSeconds(refreshTokenExpire));

        AuthenticationResponse response = new AuthenticationResponse();
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        return response;
    }
}
