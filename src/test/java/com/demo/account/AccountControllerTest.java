package com.demo.account;

import com.demo.account.exception.RequestException;
import com.demo.account.model.*;
import com.demo.account.service.*;
import com.demo.keycloak.exception.AuthenticationException;
import com.demo.keycloak.exception.KeycloakException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

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
    private AccountActivationEmailService accountActivationEmailService;

    @MockBean
    private AccountActivationService accountActivationService;

    @MockBean
    private SendPasswordResetService sendPasswordResetService;

    @MockBean
    private ResetPasswordService resetPasswordService;

    @MockBean
    private UpdateProfileService updateProfileService;

    @MockBean
    private UpdatePasswordService updatePasswordService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void getAccountStatusByUsername() throws Exception {
        when(getAccountStatusByUsernameService.getAccountStatusByUsername(anyString()))
                .thenReturn(AccountStatus.ACTIVE);

        mockMvc.perform(get("/accounts/nikkinicholas.romero@gmail.com"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        verify(getAccountStatusByUsernameService).getAccountStatusByUsername("nikkinicholas.romero@gmail.com");
    }

    @Test
    public void getAccountStatusByUsername_whenInvalid() throws Exception {
        mockMvc.perform(get("/accounts/nikkinicholas.romero"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.code").value("ConstraintViolationException"))
                .andExpect(jsonPath("$.message").value("getAccountStatusByUsername.username: must be a well-formed email address"));

        verifyNoInteractions(getAccountStatusByUsernameService);
    }

    @Test
    public void getAccountStatusByUsername_whenException() throws Exception {
        doThrow(new KeycloakException("SOME ERROR")).when(getAccountStatusByUsernameService)
                .getAccountStatusByUsername(anyString());

        mockMvc.perform(get("/accounts/nikkinicholas.romero@gmail.com"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.code").value("KeycloakException"))
                .andExpect(jsonPath("$.message").value("SOME ERROR"));

        verify(getAccountStatusByUsernameService).getAccountStatusByUsername("nikkinicholas.romero@gmail.com");
    }

    @Test
    public void createAccount() throws Exception {
        CreateAccountRequest request = new CreateAccountRequest();
        request.setUsername("nikkinicholas.romero@email.com");
        request.setPassword("Password123$");
        request.setFirstName("Nikki Nicholas");
        request.setLastName("Romero");

        mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        verify(createAccountService).createAccount(request);
    }

    @Test
    public void createAccount_whenInvalid() throws Exception {
        CreateAccountRequest request = new CreateAccountRequest();
        request.setUsername("nikkinicholas.romero");
        request.setPassword(" ");
        request.setFirstName("");
        request.setLastName(null);

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
    public void sendAccountActivationCode() throws Exception {
        mockMvc.perform(post("/accounts/nikkinicholas.romero@gmail.com/account-activation-code"))
                .andExpect(status().isAccepted());

        verify(accountActivationEmailService).sendAccountActivationCode("nikkinicholas.romero@gmail.com");
    }

    @Test
    public void sendAccountActivationCode_whenInvalid() throws Exception {
        mockMvc.perform(post("/accounts/nikkinicholas.romero/account-activation-code"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.code").value("ConstraintViolationException"))
                .andExpect(jsonPath("$.message").value("sendAccountActivationCode.username: must be a well-formed email address"));

        verifyNoInteractions(accountActivationEmailService);
    }

    @Test
    public void sendAccountActivationCode_whenError() throws Exception {
        doThrow(new KeycloakException("SOME ERROR")).when(accountActivationEmailService)
                .sendAccountActivationCode(anyString());

        mockMvc.perform(post("/accounts/nikkinicholas.romero@gmail.com/account-activation-code"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.code").value("KeycloakException"))
                .andExpect(jsonPath("$.message").value("SOME ERROR"));

        verify(accountActivationEmailService).sendAccountActivationCode("nikkinicholas.romero@gmail.com");
    }

    @Test
    public void activateAccount() throws Exception {
        mockMvc.perform(post("/accounts/nikkinicholas.romero@gmail.com?accountActivationCode=1234"))
                .andExpect(status().isAccepted());

        verify(accountActivationService).activateAccount("nikkinicholas.romero@gmail.com", "1234");
    }

    @Test
    public void activateAccount_whenInvalid() throws Exception {
        mockMvc.perform(post("/accounts/nikkinicholas.romero@gmail.com?accountActivationCode="))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.code").value("ConstraintViolationException"))
                .andExpect(jsonPath("$.message").value("activateAccount.accountActivationCode: must not be blank"));

        verifyNoInteractions(accountActivationService);
    }

    @Test
    public void activateAccount_whenError() throws Exception {
        doThrow(new KeycloakException("SOME ERROR")).when(accountActivationService).activateAccount(anyString(), anyString());

        mockMvc.perform(post("/accounts/nikkinicholas.romero@gmail.com?accountActivationCode=1234"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.code").value("KeycloakException"))
                .andExpect(jsonPath("$.message").value("SOME ERROR"));

        verify(accountActivationService).activateAccount("nikkinicholas.romero@gmail.com", "1234");
    }

    @Test
    public void sendPasswordResetCode() throws Exception {
        mockMvc.perform(post("/accounts/nikkinicholas.romero@gmail.com/password-reset"))
                .andExpect(status().isAccepted());

        verify(sendPasswordResetService).sendPasswordResetCode("nikkinicholas.romero@gmail.com");
    }

    @Test
    public void sendPasswordResetCode_whenInvalid() throws Exception {
        mockMvc.perform(post("/accounts/nikkinicholas.romero/password-reset"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.code").value("ConstraintViolationException"))
                .andExpect(jsonPath("$.message").value("sendPasswordResetCode.username: must be a well-formed email address"));

        verifyNoInteractions(sendPasswordResetService);
    }

    @Test
    public void sendPasswordResetCode_whenError() throws Exception {
        doThrow(new KeycloakException("SOME ERROR")).when(sendPasswordResetService).sendPasswordResetCode(anyString());

        mockMvc.perform(post("/accounts/nikkinicholas.romero@gmail.com/password-reset"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.code").value("KeycloakException"))
                .andExpect(jsonPath("$.message").value("SOME ERROR"));

        verify(sendPasswordResetService).sendPasswordResetCode("nikkinicholas.romero@gmail.com");
    }

    @Test
    public void resetPassword() throws Exception {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setUsername("nikkinicholas.romero@gmail.com");
        request.setPassword("Password123$");
        request.setPasswordResetCode(UUID.randomUUID().toString());

        mockMvc.perform(post("/accounts/password-reset")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted());

        verify(resetPasswordService).resetPassword(request);
    }

    @Test
    public void resetPassword_whenInvalid() throws Exception {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setUsername("nikkinicholas.romero");
        request.setPassword("");
        request.setPasswordResetCode(null);

        mockMvc.perform(post("/accounts/password-reset")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.code").value("MethodArgumentNotValidException"))
                .andExpect(jsonPath("$.message").value("password must not be blank, passwordResetCode must not be blank, username must be a well-formed email address"));

        verifyNoInteractions(resetPasswordService);
    }

    @Test
    public void resetPassword_whenError() throws Exception {
        doThrow(new KeycloakException("SOME ERROR")).when(resetPasswordService).resetPassword(any());

        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setUsername("nikkinicholas.romero@gmail.com");
        request.setPassword("Password123$");
        request.setPasswordResetCode(UUID.randomUUID().toString());

        mockMvc.perform(post("/accounts/password-reset")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.code").value("KeycloakException"))
                .andExpect(jsonPath("$.message").value("SOME ERROR"));

        verify(resetPasswordService).resetPassword(request);
    }

    @Test
    public void updateProfile() throws Exception {
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setFirstName("Nikki Nicholas");
        request.setLastName("Romero");

        String token = "some_token";

        mockMvc.perform(post("/accounts/profile")
                        .header("token", token)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted());

        verify(updateProfileService).updateProfile(token, request);
    }

    @Test
    public void updateProfile_whenInvalidBody() throws Exception {
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setFirstName("");
        request.setLastName(null);

        String token = "some_token";

        mockMvc.perform(post("/accounts/profile")
                        .header("token", token)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.code").value("MethodArgumentNotValidException"))
                .andExpect(jsonPath("$.message").value("firstName must not be blank, lastName must not be blank"));

        verifyNoInteractions(updateProfileService);
    }

    @Test
    public void updateProfile_whenInvalidHeader() throws Exception {
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setFirstName("Nikki Nicholas");
        request.setLastName("Romero");

        mockMvc.perform(post("/accounts/profile")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("MissingRequestHeaderException"))
                .andExpect(jsonPath("$.message").value("Required request header 'token' for method parameter type String is not present"));

        verifyNoInteractions(updateProfileService);
    }

    @Test
    public void updateProfile_whenError() throws Exception {
        doThrow(new RequestException("SOME ERROR")).when(updateProfileService).updateProfile(anyString(), any());

        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setFirstName("Nikki Nicholas");
        request.setLastName("Romero");

        String token = "some_token";

        mockMvc.perform(post("/accounts/profile")
                        .header("token", token)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.code").value("RequestException"))
                .andExpect(jsonPath("$.message").value("SOME ERROR"));

        verify(updateProfileService).updateProfile(token, request);
    }

    @Test
    public void updatePassword() throws Exception {
        UpdatePasswordRequest request = new UpdatePasswordRequest();
        request.setOldPassword("old_password");
        request.setNewPassword("new_password");

        String token = "some_token";

        mockMvc.perform(post("/accounts/password")
                        .header("token", token)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted());

        verify(updatePasswordService).updatePassword(token, request);
    }

    @Test
    public void updatePassword_whenInvalidBody() throws Exception {
        UpdatePasswordRequest request = new UpdatePasswordRequest();
        request.setOldPassword("");
        request.setNewPassword(null);

        String token = "some_token";

        mockMvc.perform(post("/accounts/password")
                        .header("token", token)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.code").value("MethodArgumentNotValidException"))
                .andExpect(jsonPath("$.message").value("newPassword must not be blank, oldPassword must not be blank"));

        verifyNoInteractions(updatePasswordService);
    }

    @Test
    public void updatePassword_whenInvalidHeader() throws Exception {
        UpdatePasswordRequest request = new UpdatePasswordRequest();
        request.setOldPassword("old_password");
        request.setNewPassword("new_password");

        mockMvc.perform(post("/accounts/password")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.code").value("MissingRequestHeaderException"))
                .andExpect(jsonPath("$.message").value("Required request header 'token' for method parameter type String is not present"));

        verifyNoInteractions(updatePasswordService);
    }

    @Test
    public void updatePassword_whenError() throws Exception {
        doThrow(new AuthenticationException("SOME ERROR")).when(updatePasswordService).updatePassword(anyString(), any());

        UpdatePasswordRequest request = new UpdatePasswordRequest();
        request.setOldPassword("old_password");
        request.setNewPassword("new_password");

        String token = "some_token";

        mockMvc.perform(post("/accounts/password")
                        .header("token", token)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.code").value("AuthenticationException"))
                .andExpect(jsonPath("$.message").value("SOME ERROR"));

        verify(updatePasswordService).updatePassword(token, request);
    }
}
