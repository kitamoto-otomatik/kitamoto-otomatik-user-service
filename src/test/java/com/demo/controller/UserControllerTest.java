package com.demo.controller;

import com.demo.model.AuthenticationRequest;
import com.demo.model.AuthenticationResponse;
import com.demo.model.Token;
import com.demo.orchestrator.AuthenticationOrchestrator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class UserControllerTest {
    private MockMvc mockMvc;

    @InjectMocks
    private UserController target;

    @Mock
    private AuthenticationOrchestrator orchestrator;

    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        objectMapper = new ObjectMapper();

        mockMvc = MockMvcBuilders.standaloneSetup(target).build();
    }

    @Test
    public void auth_whenSuccess() throws Exception {
        AuthenticationRequest request = new AuthenticationRequest();
        request.setType("root");
        request.setUsername("some_username");
        request.setPassword("some_password");

        Token accessToken = new Token();
        accessToken.setToken("some_accessToken");
        accessToken.setExpire(LocalDateTime.of(2020, 1, 2, 3, 4));

        Token refreshToken = new Token();
        refreshToken.setToken("some_refreshToken");
        refreshToken.setExpire(LocalDateTime.of(2021, 1, 2, 3, 4));

        AuthenticationResponse response = new AuthenticationResponse();
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);

        when(orchestrator.orchestrate(any(AuthenticationRequest.class)))
                .thenReturn(Optional.of(response));

        mockMvc.perform(post("/users/auth")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)));

        verify(orchestrator).orchestrate(request);
    }

    @Test
    public void auth_whenFailed() throws Exception {
        when(orchestrator.orchestrate(any(AuthenticationRequest.class)))
                .thenReturn(Optional.empty());

        AuthenticationRequest request = new AuthenticationRequest();
        request.setType("root");
        request.setUsername("some_username");
        request.setPassword("some_password");

        mockMvc.perform(post("/users/auth")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnauthorized());

        verify(orchestrator).orchestrate(request);
    }
}
