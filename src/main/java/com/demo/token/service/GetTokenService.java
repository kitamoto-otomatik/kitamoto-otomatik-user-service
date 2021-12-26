package com.demo.token.service;

import com.demo.keycloak.client.KeycloakTokenClient;
import com.demo.keycloak.exception.AuthenticationException;
import com.demo.token.model.GetTokenRequest;
import com.demo.token.model.Token;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.demo.ErrorMessage.KEYCLOAK_TOKEN_ERROR_MESSAGE;

@Service
public class GetTokenService {
    @Autowired
    private KeycloakTokenClient keycloakTokenClient;

    @Autowired
    private JwtTokenBuilder jwtTokenBuilder;

    public Token getToken(GetTokenRequest request) {
        String token = keycloakTokenClient.getKeycloakToken(request.getUsername(), request.getPassword());
        if (StringUtils.isNotBlank(token)) {
            return jwtTokenBuilder.buildToken(request.getUsername());
        }
        throw new AuthenticationException(KEYCLOAK_TOKEN_ERROR_MESSAGE);
    }
}
