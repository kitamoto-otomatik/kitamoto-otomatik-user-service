package com.demo.controller;

import com.demo.model.AuthenticationResponse;
import com.demo.model.Token;
import com.demo.model.TokenDecodeRequest;
import com.demo.model.TokenRefreshRequest;
import com.demo.service.TokenDecodeService;
import com.demo.service.TokenRefreshService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class TokenControllerTest {
    private static final String TOKEN = "some_token";
    private static final String REFRESH_TOKEN = "some_refresh_token";

    private MockMvc mockMvc;

    @InjectMocks
    private TokenController target;

    @Mock
    private TokenDecodeService tokenDecodeService;

    @Mock
    private TokenRefreshService tokenRefreshService;

    private ObjectMapper objectMapper;

    @Mock
    private Claims claims;

    private AuthenticationResponse authenticationResponse;

    private TokenDecodeRequest tokenDecodeRequest;

    private TokenRefreshRequest tokenRefreshRequest;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        objectMapper = new ObjectMapper();

        mockMvc = MockMvcBuilders.standaloneSetup(target).build();

        tokenDecodeRequest = new TokenDecodeRequest();
        tokenDecodeRequest.setToken(TOKEN);

        tokenRefreshRequest = new TokenRefreshRequest();
        tokenRefreshRequest.setRefreshToken(REFRESH_TOKEN);

        Token accessToken = new Token();
        accessToken.setToken("some_accessToken");
        accessToken.setExpire(LocalDateTime.of(2020, 1, 2, 3, 4));

        Token refreshToken = new Token();
        refreshToken.setToken("some_refreshToken");
        refreshToken.setExpire(LocalDateTime.of(2021, 1, 2, 3, 4));

        authenticationResponse = new AuthenticationResponse();
        authenticationResponse.setAccessToken(accessToken);
        authenticationResponse.setRefreshToken(refreshToken);
    }

    @Test
    public void decode_whenSuccess() throws Exception {
        when(tokenDecodeService.decode(anyString()))
                .thenReturn(Optional.of(claims));

        mockMvc.perform(post("/tokens/decode")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(tokenDecodeRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(claims)));

        verify(tokenDecodeService).decode(tokenDecodeRequest.getToken());
    }

    @Test
    public void decode_whenFailed() throws Exception {
        when(tokenDecodeService.decode(anyString()))
                .thenReturn(Optional.empty());

        mockMvc.perform(post("/tokens/decode")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(tokenDecodeRequest)))
                .andDo(print())
                .andExpect(status().isUnauthorized());

        verify(tokenDecodeService).decode(tokenDecodeRequest.getToken());
    }

    @Test
    public void refresh_whenSuccess() throws Exception {
        when(tokenRefreshService.refresh(anyString()))
                .thenReturn(Optional.of(authenticationResponse));

        mockMvc.perform(post("/tokens/refresh")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(tokenRefreshRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(authenticationResponse)));

        verify(tokenRefreshService).refresh(tokenRefreshRequest.getRefreshToken());
    }

    @Test
    public void refresh_whenFailed() throws Exception {
        when(tokenRefreshService.refresh(anyString()))
                .thenReturn(Optional.empty());

        mockMvc.perform(post("/tokens/refresh")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(tokenRefreshRequest)))
                .andDo(print())
                .andExpect(status().isUnauthorized());

        verify(tokenRefreshService).refresh(tokenRefreshRequest.getRefreshToken());
    }
}
