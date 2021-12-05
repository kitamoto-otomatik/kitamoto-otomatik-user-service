package com.demo.account;

import com.demo.account.exception.KeycloakException;
import com.demo.account.model.AccountStatus;
import com.demo.account.model.CreateAccountRequest;
import com.demo.account.service.CreateAccountService;
import com.demo.account.service.GetAccountStatusByUsernameService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AccountController.class)
public class AccountControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private GetAccountStatusByUsernameService getAccountStatusByUsernameService;

    @MockBean
    private CreateAccountService createAccountService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void getAccountStatusByUsername_whenOk() throws Exception {
        when(getAccountStatusByUsernameService.getAccountStatusByUsername(anyString()))
                .thenReturn(AccountStatus.ACTIVE);

        mockMvc.perform(get("/accounts/nikkinicholas.romero@gmail.com"))
                .andDo(print())
                .andExpect(status().isOk());

        verify(getAccountStatusByUsernameService).getAccountStatusByUsername("nikkinicholas.romero@gmail.com");
    }

    @Test
    public void getAccountStatusByUsername_whenException() throws Exception {
        doThrow(new KeycloakException("SOME ERROR")).when(getAccountStatusByUsernameService).getAccountStatusByUsername(anyString());

        mockMvc.perform(get("/accounts/nikkinicholas.romero@gmail.com"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.code").value("KeycloakException"))
                .andExpect(jsonPath("$.message").value("SOME ERROR"));

        verify(getAccountStatusByUsernameService).getAccountStatusByUsername("nikkinicholas.romero@gmail.com");
    }

    @Test
    public void createAccount_whenOk() throws Exception {
        CreateAccountRequest request = new CreateAccountRequest();
        request.setUsername("someUsername");
        request.setPassword("somePassword");
        request.setFirstName("someFirstname");
        request.setLastName("someLastname");

        mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        verify(createAccountService).createAccount(request);
    }

    @Test
    public void createAccount_whenException() throws Exception {
        doThrow(new KeycloakException("SOME ERROR")).when(createAccountService).createAccount(any(CreateAccountRequest.class));

        CreateAccountRequest request = new CreateAccountRequest();
        request.setUsername("someUsername");
        request.setPassword("somePassword");
        request.setFirstName("someFirstname");
        request.setLastName("someLastname");

        mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.code").value("KeycloakException"))
                .andExpect(jsonPath("$.message").value("SOME ERROR"));

        verify(createAccountService).createAccount(request);
    }
}
