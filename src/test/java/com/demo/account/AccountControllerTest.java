package com.demo.account;

import com.demo.account.exception.KeycloakException;
import com.demo.account.model.AccountStatus;
import com.demo.account.model.CreateAccountRequest;
import com.demo.account.service.AccountActivationEmailService;
import com.demo.account.service.AccountActivationService;
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

    @MockBean
    private AccountActivationService accountActivationService;

    @MockBean
    private AccountActivationEmailService activationEmailService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void getAccountStatusByUsername_whenOk() throws Exception {
        when(getAccountStatusByUsernameService.getAccountStatusByUsername(anyString()))
                .thenReturn(AccountStatus.ACTIVE);

        mockMvc.perform(get("/accounts/nikkinicholas.romero@gmail.com"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        verify(getAccountStatusByUsernameService).getAccountStatusByUsername("nikkinicholas.romero@gmail.com");
    }

    @Test
    public void getAccountStatusByUsername_whenInvalidEmail() throws Exception {
        when(getAccountStatusByUsernameService.getAccountStatusByUsername(anyString()))
                .thenReturn(AccountStatus.ACTIVE);

        mockMvc.perform(get("/accounts/nikkinicholas.romero"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.code").value("ConstraintViolationException"))
                .andExpect(jsonPath("$.message").value("getAccountStatusByUsername.username: must be a well-formed email address"));

        verifyNoInteractions(getAccountStatusByUsernameService);
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
        request.setUsername("someUsername@email.com");
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
    public void createAccount_whenRequestIsInvalid() throws Exception {
        CreateAccountRequest request = new CreateAccountRequest();
        request.setUsername("invalidemail");
        request.setPassword("");
        request.setFirstName("");
        request.setLastName("");

        mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.code").value("MethodArgumentNotValidException"))
                .andExpect(jsonPath("$.message").value("firstName must not be blank, lastName must not be blank, password must not be blank, username must be a well-formed email address"));

        verifyNoInteractions(createAccountService);
    }

    @Test
    public void createAccount_whenException() throws Exception {
        doThrow(new KeycloakException("SOME ERROR")).when(createAccountService).createAccount(any(CreateAccountRequest.class));

        CreateAccountRequest request = new CreateAccountRequest();
        request.setUsername("someUsername@email.com");
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

    @Test
    public void resendActivationCode_whenOk() throws Exception {
        mockMvc.perform(post("/accounts/nikkinicholas.romero@gmail.com/resendActivationCode"))
                .andExpect(status().isAccepted());

        verify(activationEmailService).resendActivationCode("nikkinicholas.romero@gmail.com");
    }

    @Test
    public void resendActivationCode_whenError() throws Exception {
        doThrow(new KeycloakException("SOME ERROR")).when(activationEmailService).resendActivationCode(anyString());

        mockMvc.perform(post("/accounts/nikkinicholas.romero@gmail.com/resendActivationCode"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.code").value("KeycloakException"))
                .andExpect(jsonPath("$.message").value("SOME ERROR"));

        verify(activationEmailService).resendActivationCode("nikkinicholas.romero@gmail.com");
    }

    @Test
    public void activateAccount_whenOk() throws Exception {
        mockMvc.perform(post("/accounts/nikkinicholas.romero@gmail.com?activationCode=1234"))
                .andExpect(status().isAccepted());

        verify(accountActivationService).activateAccount("nikkinicholas.romero@gmail.com", "1234");
    }

    @Test
    public void activateAccount_whenError() throws Exception {
        doThrow(new KeycloakException("SOME ERROR")).when(accountActivationService).activateAccount(anyString(), anyString());

        mockMvc.perform(post("/accounts/nikkinicholas.romero@gmail.com?activationCode=1234"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.code").value("KeycloakException"))
                .andExpect(jsonPath("$.message").value("SOME ERROR"));

        verify(accountActivationService).activateAccount("nikkinicholas.romero@gmail.com", "1234");
    }

    @Test
    public void activateAccount_whenInvalidRequest() throws Exception {
        mockMvc.perform(post("/accounts/nikkinicholas.romero@gmail.com?activationCode="))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.code").value("ConstraintViolationException"))
                .andExpect(jsonPath("$.message").value("activateAccount.activationCode: must not be blank"));

        verifyNoInteractions(accountActivationService);
    }
}
