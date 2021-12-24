package com.demo.account.service;

import com.demo.account.client.KeycloakUserClient;
import com.demo.account.client.MailClient;
import com.demo.account.exception.RequestException;
import com.demo.account.model.AccountActivationTemplateVariables;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class CreateAccountServiceTest {
    private static final String ACTIVATION_CODE = "activationCode";

    @InjectMocks
    private CreateAccountService target;

    @Mock
    private GetAccountStatusByUsernameService getAccountStatusByUsernameService;

    @Mock
    private KeycloakUserClient keycloakUserClient;

    @Mock
    private AccountActivationEmailService accountActivationEmailService;

    @Mock
    private MailClient<AccountActivationTemplateVariables> mailClient;

    @Captor
    private ArgumentCaptor<KeycloakUser> keycloakUserArgumentCaptor;

    @Captor
    private ArgumentCaptor<String> activationCodeCaptor;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        ReflectionTestUtils.setField(target, "code", ACTIVATION_CODE);
    }

    @Test
    public void createAccount_whenAccountAlreadyExists() {
        when(getAccountStatusByUsernameService.getAccountStatusByUsername(anyString()))
                .thenReturn(AccountStatus.UNVERIFIED);

        CreateAccountRequest request = new CreateAccountRequest();
        request.setUsername("someUsername");
        request.setPassword("somePassword");
        request.setFirstName("someFirstname");
        request.setLastName("someLastname");

        RequestException e = assertThrows(RequestException.class, () ->
            target.createAccount(request)
        );

        assertThat(e.getMessage()).isEqualTo(USERNAME_IS_ALREADY_TAKEN_ERROR_MESSAGE);

        verify(getAccountStatusByUsernameService).getAccountStatusByUsername("someUsername");
        verifyNoInteractions(keycloakUserClient);
        verifyNoInteractions(accountActivationEmailService);
    }

    @Test
    public void createAccount_whenValid() {
        when(getAccountStatusByUsernameService.getAccountStatusByUsername(anyString()))
                .thenReturn(AccountStatus.UNREGISTERED);

        CreateAccountRequest request = new CreateAccountRequest();
        request.setUsername("someUsername");
        request.setPassword("somePassword");
        request.setFirstName("someFirstname");
        request.setLastName("someLastname");

        target.createAccount(request);

        verify(getAccountStatusByUsernameService).getAccountStatusByUsername("someUsername");

        verify(keycloakUserClient).createAccount(keycloakUserArgumentCaptor.capture());
        KeycloakUser keycloakUser = keycloakUserArgumentCaptor.getValue();
        assertThat(keycloakUser).isNotNull();
        assertThat(keycloakUser.getUsername()).isEqualTo("someUsername");
        assertThat(keycloakUser.getEmail()).isEqualTo("someUsername");
        assertThat(keycloakUser.getFirstName()).isEqualTo("someFirstname");
        assertThat(keycloakUser.getLastName()).isEqualTo("someLastname");
        assertThat(keycloakUser.getAttributes()).isNotEmpty();
        assertThat(keycloakUser.getAttributes().size()).isEqualTo(1);
        assertThat(keycloakUser.getAttributes().containsKey(ACTIVATION_CODE)).isTrue();
        assertThat(keycloakUser.getAttributes().get(ACTIVATION_CODE)).isNotEmpty();
        assertThat(keycloakUser.getAttributes().get(ACTIVATION_CODE).get(0)).isNotBlank();
        assertThat(keycloakUser.getCredentials()).isNotEmpty();
        assertThat(keycloakUser.getCredentials().size()).isEqualTo(1);
        assertThat(keycloakUser.getCredentials().get(0)).isNotNull();
        assertThat(keycloakUser.getCredentials().get(0).getType()).isEqualTo("password");
        assertThat(keycloakUser.getCredentials().get(0).getValue()).isEqualTo("somePassword");
        assertThat(keycloakUser.getCredentials().get(0).isTemporary()).isFalse();
        assertThat(keycloakUser.isEmailVerified()).isFalse();
        assertThat(keycloakUser.isEnabled()).isFalse();

        verify(accountActivationEmailService).sendActivationCode(eq("someUsername"), activationCodeCaptor.capture());
        String activationCode = activationCodeCaptor.getValue();
        assertThat(activationCode).isNotBlank();
        assertThat(activationCode).isEqualTo(keycloakUser.getAttributes().get(ACTIVATION_CODE).get(0));
    }
}
