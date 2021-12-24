package com.demo.account;

import com.demo.account.model.CreateAccountRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("mock")
public class AccountControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void getAccountStatusByUsername() throws Exception {
        this.mockMvc.perform(get("/accounts/nikkinicholas.romero@gmail.com"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.status").value("UNVERIFIED"))
                .andExpect(status().isOk());
    }

    @Test
    public void createAccount() throws Exception {
        CreateAccountRequest request = new CreateAccountRequest();
        request.setUsername("someUsername@email.com");
        request.setPassword("somePassword");
        request.setFirstName("someFirstname");
        request.setLastName("someLastname");

        this.mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    public void resendActivationCode() throws Exception {
        this.mockMvc.perform(post("/accounts/nikkinicholas.romero@gmail.com/resendActivationCode"))
                .andExpect(status().isAccepted());
    }

    @Test
    public void activateAccount() throws Exception {
        this.mockMvc.perform(post("/accounts/nikkinicholas.romero@gmail.com?activationCode=1234"))
                .andExpect(status().isAccepted());
    }

    @Test
    public void forgotPassword() throws Exception {
        this.mockMvc.perform(post("/accounts/sayin.leslieanne@gmail.com/password/forgot"))
                .andExpect(status().isAccepted());
    }
}
