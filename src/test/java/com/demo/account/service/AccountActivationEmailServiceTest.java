package com.demo.account.service;

import com.demo.account.exception.RequestException;
import com.demo.keycloak.client.KeycloakUserClient;
import com.demo.keycloak.model.KeycloakAccountAttributeUpdateRequest;
import com.demo.keycloak.model.KeycloakUser;
import com.demo.mail.client.MailClient;
import com.demo.mail.model.AccountActivationTemplateVariables;
import com.demo.mail.model.Mail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static com.demo.ErrorMessage.ACCOUNT_IS_ALREADY_ACTIVATED_ERROR_MESSAGE;
import static com.demo.ErrorMessage.USERNAME_DOES_NOT_EXIST_ERROR_MESSAGE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class AccountActivationEmailServiceTest {
    private static final String ACCOUNT_ACTIVATION_URL = "http://localhost:4200/accountActivation?emailAddress=%s&accountActivationCode=%s";
    private static final String ACCOUNT_ACTIVATION_CODE = "accountActivationCode";
    private static final String ACCOUNT_ACTIVATION_EMAIL_SENDER = "nikkinicholas.romero@gmail.com";
    private static final String ACCOUNT_ACTIVATION_EMAIL_SUBJECT = "Account Activation";
    private static final String ACCOUNT_ACTIVATION_EMAIL_TEMPLATE = "account_activation";

    @InjectMocks
    private AccountActivationEmailService target;

    @Mock
    private Random random;

    @Mock
    private MailClient<AccountActivationTemplateVariables> mailClient;

    @Mock
    private KeycloakUserClient keycloakUserClient;

    @Captor
    private ArgumentCaptor<KeycloakAccountAttributeUpdateRequest> keycloakAccountAttributeUpdateRequestArgumentCaptor;

    @Captor
    private ArgumentCaptor<Mail<AccountActivationTemplateVariables>> mailArgumentCaptor;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        ReflectionTestUtils.setField(target, "accountActivationUrl", ACCOUNT_ACTIVATION_URL);
        ReflectionTestUtils.setField(target, "accountActivationCode", ACCOUNT_ACTIVATION_CODE);
        ReflectionTestUtils.setField(target, "accountActivationEmailSender", ACCOUNT_ACTIVATION_EMAIL_SENDER);
        ReflectionTestUtils.setField(target, "accountActivationEmailSubject", ACCOUNT_ACTIVATION_EMAIL_SUBJECT);
        ReflectionTestUtils.setField(target, "accountActivationEmailTemplate", ACCOUNT_ACTIVATION_EMAIL_TEMPLATE);

        when(random.nextInt(1_000_000)).thenReturn(12345);
    }

    @Test
    public void sendAccountActivationCode_whenUsernameDoesNotExist() {
        when(keycloakUserClient.getUserByUsername(anyString())).thenReturn(Optional.empty());

        RequestException e = assertThrows(RequestException.class, () ->
                target.sendAccountActivationCode("non-existent@gmail.com"));
        assertThat(e.getMessage()).isEqualTo(USERNAME_DOES_NOT_EXIST_ERROR_MESSAGE);
        verify(keycloakUserClient).getUserByUsername("non-existent@gmail.com");
        verify(keycloakUserClient, never()).updateUser(anyString(), any());
        verifyNoInteractions(mailClient);
    }

    @Test
    public void sendAccountActivationCode_whenUserIsAlreadyActivated() {
        Map<String, List<String>> attributes = new HashMap<>();
        attributes.put(ACCOUNT_ACTIVATION_CODE, Collections.singletonList("abcdef"));

        KeycloakUser keycloakUser = new KeycloakUser();
        keycloakUser.setId("someId");
        keycloakUser.setUsername("nikkinicholas.romero@gmail.com");
        keycloakUser.setAttributes(attributes);
        keycloakUser.setEmailVerified(true);
        when(keycloakUserClient.getUserByUsername(anyString())).thenReturn(Optional.of(keycloakUser));

        RequestException e = assertThrows(RequestException.class, () ->
                target.sendAccountActivationCode("nikkinicholas.romero@gmail.com"));
        assertThat(e.getMessage()).isEqualTo(ACCOUNT_IS_ALREADY_ACTIVATED_ERROR_MESSAGE);
        verify(keycloakUserClient).getUserByUsername("nikkinicholas.romero@gmail.com");
        verify(keycloakUserClient, never()).updateUser(anyString(), any());
        verifyNoInteractions(mailClient);
    }

    @Test
    public void sendAccountActivationCode() {
        Map<String, List<String>> attributes = new HashMap<>();
        attributes.put(ACCOUNT_ACTIVATION_CODE, Collections.singletonList("abcdef"));

        KeycloakUser keycloakUser = new KeycloakUser();
        keycloakUser.setId("someId");
        keycloakUser.setUsername("nikkinicholas.romero@gmail.com");
        keycloakUser.setAttributes(attributes);
        when(keycloakUserClient.getUserByUsername(anyString())).thenReturn(Optional.of(keycloakUser));

        target.sendAccountActivationCode("nikkinicholas.romero@gmail.com");

        verify(keycloakUserClient).getUserByUsername("nikkinicholas.romero@gmail.com");
        verify(keycloakUserClient).updateUser(eq("someId"), keycloakAccountAttributeUpdateRequestArgumentCaptor.capture());
        KeycloakAccountAttributeUpdateRequest keycloakAccountAttributeUpdateRequest = keycloakAccountAttributeUpdateRequestArgumentCaptor.getValue();
        assertThat(keycloakAccountAttributeUpdateRequest).isNotNull();
        assertThat(keycloakAccountAttributeUpdateRequest.getAttributes()).isNotEmpty();
        assertThat(keycloakAccountAttributeUpdateRequest.getAttributes().get(ACCOUNT_ACTIVATION_CODE)).isNotEmpty();
        verify(mailClient).sendEmail(mailArgumentCaptor.capture());
        Mail<AccountActivationTemplateVariables> mail = mailArgumentCaptor.getValue();
        assertThat(mail).isNotNull();
        assertThat(mail.getFrom()).isEqualTo(ACCOUNT_ACTIVATION_EMAIL_SENDER);
        assertThat(mail.getTo()).isEqualTo("nikkinicholas.romero@gmail.com");
        assertThat(mail.getSubject()).isEqualTo(ACCOUNT_ACTIVATION_EMAIL_SUBJECT);
        assertThat(mail.getTemplate()).isEqualTo(ACCOUNT_ACTIVATION_EMAIL_TEMPLATE);
        assertThat(mail.getTemplateVariables()).isNotNull();
        assertThat(mail.getTemplateVariables().getAccountActivationLink()).isEqualTo("http://localhost:4200/accountActivation?emailAddress=nikkinicholas.romero@gmail.com&accountActivationCode=12345");
    }
}
