package com.demo.integration;

import com.demo.model.AuthenticationRequest;
import com.demo.model.AuthenticationResponse;
import com.demo.model.TokenDecodeRequest;
import com.demo.model.TokenRefreshRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("mock")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class IntegrationTest {
//    @Autowired
//    private MockMvc mockMvc;
//
//    private ObjectMapper objectMapper;
//
//    @BeforeEach
//    public void setup() {
//        objectMapper = new ObjectMapper();
//    }
//
//    @Test
//    public void auth_decode_refresh() throws Exception {
//        AuthenticationRequest request = new AuthenticationRequest();
//        request.setType("root");
//        request.setUsername("nikkinicholas@gmail.com");
//        request.setPassword("nikki_password");
//
//        MvcResult result = mockMvc.perform(post("/users/auth")
//                .contentType("application/json")
//                .content(objectMapper.writeValueAsString(request)))
//                .andDo(print())
//                .andExpect(status().isOk())
//                .andReturn();
//
//        AuthenticationResponse authenticationResponse = objectMapper.readValue(result.getResponse().getContentAsString(), AuthenticationResponse.class);
//        assertThat(authenticationResponse).isNotNull();
//
//        TokenDecodeRequest tokenDecodeRequest = new TokenDecodeRequest();
//        tokenDecodeRequest.setToken(authenticationResponse.getAccessToken().getToken());
//
//        result = mockMvc.perform(post("/tokens/decode")
//                .contentType("application/json")
//                .content(objectMapper.writeValueAsString(tokenDecodeRequest)))
//                .andDo(print())
//                .andExpect(status().isOk())
//                .andReturn();
//
//        TokenRefreshRequest tokenRefreshRequest = new TokenRefreshRequest();
//        tokenRefreshRequest.setRefreshToken(authenticationResponse.getRefreshToken().getToken());
//
//        result = mockMvc.perform(post("/tokens/refresh")
//                .contentType("application/json")
//                .content(objectMapper.writeValueAsString(tokenRefreshRequest)))
//                .andDo(print())
//                .andExpect(status().isOk())
//                .andReturn();
//
//        authenticationResponse = objectMapper.readValue(result.getResponse().getContentAsString(), AuthenticationResponse.class);
//        assertThat(authenticationResponse).isNotNull();
//    }
}
