package com.demo.service;

import com.demo.model.AuthenticationResponse;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class TokenRefreshService {
    @Autowired
    private TokenDecodeService tokenDecodeService;

    @Autowired
    private AuthenticationResponseBuilder authenticationResponseBuilder;

    public Optional<AuthenticationResponse> refresh(String refreshToken) {
        Optional<Claims> optionalClaims = tokenDecodeService.decode(refreshToken);
        if (optionalClaims.isPresent()) {
            AuthenticationResponse authenticationResponse =
                    authenticationResponseBuilder.buildAuthenticationResponse(optionalClaims.get().getSubject());
            return Optional.of(authenticationResponse);
        }

        return Optional.empty();
    }
}
