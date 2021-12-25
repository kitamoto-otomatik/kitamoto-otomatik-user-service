package com.demo.account.service;

import com.demo.account.client.KeycloakUserClient;
import com.demo.account.exception.RequestException;
import com.demo.account.model.AccountStatus;
import com.demo.account.model.CreateAccountRequest;
import com.demo.account.model.KeycloakUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.test.util.ReflectionTestUtils;

import static com.demo.ErrorMessage.USERNAME_IS_ALREADY_TAKEN_ERROR_MESSAGE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class CreateAccountServiceTest {
    private static final String ACCOUNT_ACTIVATION_CODE = "accountActivationCode";

    @InjectMocks
    private CreateAccountService target;

    @Mock
    private GetAccountStatusByUsernameService getAccountStatusByUsernameService;

    @Mock
    private KeycloakUserClient keycloakUserClient;

    @Mock
    private AccountActivationEmailService accountActivationEmailService;

    @Captor
    private ArgumentCaptor<KeycloakUser> keycloakUserArgumentCaptor;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        ReflectionTestUtils.setField(target, "accountActivationCode", ACCOUNT_ACTIVATION_CODE);
    }

    @Test
    public void createAccount() {
        when(getAccountStatusByUsernameService.getAccountStatusByUsername(anyString()))
                .thenReturn(AccountStatus.UNREGISTERED);

        CreateAccountRequest request = new CreateAccountRequest();
        request.setUsername("nikkinicholas.romero@gmail.com");
        request.setPassword("Password123$");
        request.setFirstName("Nikki Nicholas");
        request.setLastName("Romero");

        target.createAccount(request);

        verify(getAccountStatusByUsernameService).getAccountStatusByUsername("nikkinicholas.romero@gmail.com");

        verify(keycloakUserClient).createAccount(keycloakUserArgumentCaptor.capture());
        KeycloakUser keycloakUser = keycloakUserArgumentCaptor.getValue();
        assertThat(keycloakUser).isNotNull();
        assertThat(keycloakUser.getUsername()).isEqualTo("nikkinicholas.romero@gmail.com");
        assertThat(keycloakUser.getEmail()).isEqualTo("nikkinicholas.romero@gmail.com");
        assertThat(keycloakUser.getFirstName()).isEqualTo("Nikki Nicholas");
        assertThat(keycloakUser.getLastName()).isEqualTo("Romero");
        assertThat(keycloakUser.getAttributes()).isNull();
        assertThat(keycloakUser.getCredentials()).isNotEmpty();
        assertThat(keycloakUser.getCredentials().size()).isEqualTo(1);
        assertThat(keycloakUser.getCredentials().get(0)).isNotNull();
        assertThat(keycloakUser.getCredentials().get(0).getType()).isEqualTo("password");
        assertThat(keycloakUser.getCredentials().get(0).getValue()).isEqualTo("Password123$");
        assertThat(keycloakUser.getCredentials().get(0).isTemporary()).isFalse();
        assertThat(keycloakUser.isEmailVerified()).isFalse();
        assertThat(keycloakUser.isEnabled()).isFalse();

        verify(accountActivationEmailService).sendAccountActivationCode(eq("nikkinicholas.romero@gmail.com"));
    }

    @Test
    public void createAccount_whenAccountAlreadyExists() {
        when(getAccountStatusByUsernameService.getAccountStatusByUsername(anyString()))
                .thenReturn(AccountStatus.UNVERIFIED);

        CreateAccountRequest request = new CreateAccountRequest();
        request.setUsername("nikkinicholas.romero@gmail.com");
        request.setPassword("Password123$");
        request.setFirstName("Nikki Nicholas");
        request.setLastName("Romero");

        RequestException e = assertThrows(RequestException.class, () ->
                target.createAccount(request));

        assertThat(e.getMessage()).isEqualTo(USERNAME_IS_ALREADY_TAKEN_ERROR_MESSAGE);

        verify(getAccountStatusByUsernameService).getAccountStatusByUsername("nikkinicholas.romero@gmail.com");
        verifyNoInteractions(keycloakUserClient);
        verifyNoInteractions(accountActivationEmailService);
    }
}
