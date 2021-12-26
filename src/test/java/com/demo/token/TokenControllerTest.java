package com.demo.token;

import com.demo.keycloak.exception.AuthenticationException;
import com.demo.token.model.GetTokenRequest;
import com.demo.token.model.Token;
import com.demo.token.model.ValidateTokenRequest;
import com.demo.token.service.GetTokenService;
import com.demo.token.service.TokenValidationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TokenController.class)
public class TokenControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private GetTokenService getTokenService;

    @MockBean
    private TokenValidationService tokenValidationService;

    private Token token;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        token = new Token();
        token.setToken("someToken");
        token.setExpire(LocalDateTime.of(2021, 12, 26, 6, 20, 35));
    }

    @Test
    public void getToken() throws Exception {
        GetTokenRequest request = new GetTokenRequest();
        request.setUsername("nikkinicholas.romero@gmail.com");
        request.setPassword("Password123$");

        when(getTokenService.getToken(any(GetTokenRequest.class)))
                .thenReturn(token);

        mockMvc.perform(post("/token")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.token").value("someToken"))
                .andExpect(jsonPath("$.expire").value("2021-12-26 06:20:35"));

        verify(getTokenService).getToken(request);
    }

    @Test
    public void getToken_whenInvalid() throws Exception {
        GetTokenRequest request = new GetTokenRequest();
        request.setUsername("");
        request.setPassword(null);

        when(getTokenService.getToken(any(GetTokenRequest.class)))
                .thenReturn(token);

        mockMvc.perform(post("/token")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.code").value("MethodArgumentNotValidException"))
                .andExpect(jsonPath("$.message").value("password must not be blank, username must not be blank"));

        verifyNoInteractions(getTokenService);
    }

    @Test
    public void getToken_whenError() throws Exception {
        GetTokenRequest request = new GetTokenRequest();
        request.setUsername("nikkinicholas.romero@gmail.com");
        request.setPassword("Password123$");

        doThrow(new AuthenticationException("SOME ERROR")).when(getTokenService)
                .getToken(any());

        mockMvc.perform(post("/token")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.code").value("AuthenticationException"))
                .andExpect(jsonPath("$.message").value("SOME ERROR"));

        verify(getTokenService).getToken(request);
    }

    @Test
    public void validateToken() throws Exception {
        ValidateTokenRequest request = new ValidateTokenRequest();
        request.setToken("someToken");

        mockMvc.perform(post("/token/validate")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(tokenValidationService).validate(request);
    }

    @Test
    public void validateToken_whenInvalid() throws Exception {
        ValidateTokenRequest request = new ValidateTokenRequest();
        request.setToken("");

        mockMvc.perform(post("/token/validate")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.code").value("MethodArgumentNotValidException"))
                .andExpect(jsonPath("$.message").value("token must not be blank"));

        verifyNoInteractions(tokenValidationService);
    }

    @Test
    public void validateToken_whenError() throws Exception {
        ValidateTokenRequest request = new ValidateTokenRequest();
        request.setToken("someInvalid");

        doThrow(new AuthenticationException("SOME ERROR")).when(tokenValidationService)
                .validate(any());

        mockMvc.perform(post("/token/validate")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.code").value("AuthenticationException"))
                .andExpect(jsonPath("$.message").value("SOME ERROR"));

        verify(tokenValidationService).validate(request);
    }
}
