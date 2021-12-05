package com.demo.account.service;

import com.demo.account.client.KeycloakUserClient;
import com.demo.account.client.MailClient;
import com.demo.account.model.CreateAccountRequest;
import com.demo.account.model.KeycloakUser;
import com.demo.account.model.Mail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

public class CreateAccountServiceTest {
    private static final String ACCOUNT_ACTIVATION_URL = "http://localhost:4200/accountActivation?emailAddress=%s&activationCode=%s";
    private static final String ACCOUNT_ACTIVATION_EMAIL_SENDER = "someAccountActivationEmailSender";
    private static final String ACCOUNT_ACTIVATION_EMAIL_SUBJECT = "someAccountActivationEmailSubject";
    private static final String ACCOUNT_ACTIVATION_EMAIL_BODY = "someAccountActivationEmailBody";
    private static final String ACCOUNT_ACTIVATION_EMAIL_TEMPLATE = "someAccountActivationEmailTemplate";

    @InjectMocks
    private CreateAccountService target;

    @Mock
    private KeycloakUserClient keycloakUserClient;

    @Mock
    private MailClient mailClient;

    @Captor
    private ArgumentCaptor<KeycloakUser> keycloakUserArgumentCaptor;

    @Captor
    private ArgumentCaptor<Mail> mailArgumentCaptor;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        ReflectionTestUtils.setField(target, "accountActivationUrl", ACCOUNT_ACTIVATION_URL);
        ReflectionTestUtils.setField(target, "accountActivationEmailSender", ACCOUNT_ACTIVATION_EMAIL_SENDER);
        ReflectionTestUtils.setField(target, "accountActivationEmailSubject", ACCOUNT_ACTIVATION_EMAIL_SUBJECT);
        ReflectionTestUtils.setField(target, "accountActivationEmailBody", ACCOUNT_ACTIVATION_EMAIL_BODY);
        ReflectionTestUtils.setField(target, "accountActivationEmailTemplate", ACCOUNT_ACTIVATION_EMAIL_TEMPLATE);
    }

    @Test
    public void createAccount() {
        CreateAccountRequest request = new CreateAccountRequest();
        request.setUsername("someUsername");
        request.setPassword("somePassword");
        request.setFirstName("someFirstname");
        request.setLastName("someLastname");

        target.createAccount(request);

        verify(keycloakUserClient).createAccount(keycloakUserArgumentCaptor.capture());
        KeycloakUser keycloakUser = keycloakUserArgumentCaptor.getValue();
        assertThat(keycloakUser).isNotNull();
        assertThat(keycloakUser.getUsername()).isEqualTo("someUsername");
        assertThat(keycloakUser.getEmail()).isEqualTo("someUsername");
        assertThat(keycloakUser.getFirstName()).isEqualTo("someFirstname");
        assertThat(keycloakUser.getLastName()).isEqualTo("someLastname");
        assertThat(keycloakUser.getAttributes()).isNotEmpty();
        assertThat(keycloakUser.getAttributes().size()).isEqualTo(1);
        assertThat(keycloakUser.getAttributes().containsKey("verificationCode")).isTrue();
        assertThat(keycloakUser.getAttributes().get("verificationCode")).isNotEmpty();
        assertThat(keycloakUser.getAttributes().get("verificationCode").get(0)).isNotBlank();
        assertThat(keycloakUser.getCredentials()).isNotEmpty();
        assertThat(keycloakUser.getCredentials().size()).isEqualTo(1);
        assertThat(keycloakUser.getCredentials().get(0)).isNotNull();
        assertThat(keycloakUser.getCredentials().get(0).getType()).isEqualTo("password");
        assertThat(keycloakUser.getCredentials().get(0).getValue()).isEqualTo("somePassword");
        assertThat(keycloakUser.getCredentials().get(0).isTemporary()).isFalse();
        assertThat(keycloakUser.isEmailVerified()).isFalse();
        assertThat(keycloakUser.isEnabled()).isFalse();

        verify(mailClient).sendEmail(mailArgumentCaptor.capture());
        Mail mail = mailArgumentCaptor.getValue();
        assertThat(mail).isNotNull();
        assertThat(mail.getFrom()).isEqualTo(ACCOUNT_ACTIVATION_EMAIL_SENDER);
        assertThat(mail.getTo()).isEqualTo("someUsername");
        assertThat(mail.getSubject()).isEqualTo(ACCOUNT_ACTIVATION_EMAIL_SUBJECT);
        assertThat(mail.getBody()).isEqualTo(ACCOUNT_ACTIVATION_EMAIL_BODY);
        assertThat(mail.getTemplate()).isEqualTo(ACCOUNT_ACTIVATION_EMAIL_TEMPLATE);
        assertThat(mail.getTemplateVariables()).isNotNull();
        assertThat(mail.getTemplateVariables().getAccountActivationLink()).startsWith("http://localhost:4200/accountActivation?emailAddress=someUsername&activationCode=");
    }
}
