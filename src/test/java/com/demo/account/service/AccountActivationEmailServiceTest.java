package com.demo.account.service;

import com.demo.account.client.KeycloakUserClient;
import com.demo.account.client.MailClient;
import com.demo.account.exception.KeycloakException;
import com.demo.account.exception.RequestException;
import com.demo.account.model.AccountActivationTemplateVariables;
import com.demo.account.model.KeycloakAccountAttributeUpdateRequest;
import com.demo.account.model.KeycloakUser;
import com.demo.account.model.Mail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static com.demo.ErrorMessage.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class AccountActivationEmailServiceTest {
    private static final String ACCOUNT_ACTIVATION_URL = "http://localhost:4200/accountActivation?emailAddress=%s&accountActivationCode=%s";
    private static final String ACCOUNT_ACTIVATION_CODE = "accountActivationCode";
    private static final String ACCOUNT_ACTIVATION_EMAIL_SENDER = "nikkinicholas.romero@gmail.com";
    private static final String ACCOUNT_ACTIVATION_EMAIL_SUBJECT = "Account Activation";
    private static final String ACCOUNT_ACTIVATION_EMAIL_BODY = "Activate your account by clicking the link below.";
    private static final String ACCOUNT_ACTIVATION_EMAIL_TEMPLATE = "account_activation";

    @InjectMocks
    private AccountActivationEmailService target;

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
        ReflectionTestUtils.setField(target, "accountActivationEmailBody", ACCOUNT_ACTIVATION_EMAIL_BODY);
        ReflectionTestUtils.setField(target, "accountActivationEmailTemplate", ACCOUNT_ACTIVATION_EMAIL_TEMPLATE);
    }

    @Test
    public void sendAccountActivationCode_whenUsernameDoesNotExist() {
        Map<String, List<String>> attributes = new HashMap<>();
        attributes.put(ACCOUNT_ACTIVATION_CODE, Collections.singletonList("abcdef"));

        KeycloakUser keycloakUser1 = new KeycloakUser();
        keycloakUser1.setId("someId");
        keycloakUser1.setUsername("nikkinicholas.romero@gmail.com");
        keycloakUser1.setAttributes(attributes);
        KeycloakUser keycloakUser2 = new KeycloakUser();
        keycloakUser2.setUsername("sayin.leslieanne@gmail.com");
        List<KeycloakUser> keycloakUserList = new ArrayList<>();
        keycloakUserList.add(keycloakUser1);
        keycloakUserList.add(keycloakUser2);
        when(keycloakUserClient.getUserListByUsername(anyString())).thenReturn(keycloakUserList);

        RequestException e = assertThrows(RequestException.class, () ->
                target.sendAccountActivationCode("non-existent@gmail.com"));
        assertThat(e.getMessage()).isEqualTo(USERNAME_DOES_NOT_EXIST_ERROR_MESSAGE);
        verify(keycloakUserClient).getUserListByUsername("non-existent@gmail.com");
        verify(keycloakUserClient, never()).updateKeycloakAccountAttribute(anyString(), any());
        verifyNoInteractions(mailClient);
    }

    @Test
    public void sendAccountActivationCode_whenMultipleUsersMatchTheUsername() {
        Map<String, List<String>> attributes = new HashMap<>();
        attributes.put(ACCOUNT_ACTIVATION_CODE, Collections.singletonList("abcdef"));

        KeycloakUser keycloakUser1 = new KeycloakUser();
        keycloakUser1.setId("someId");
        keycloakUser1.setUsername("nikkinicholas.romero@gmail.com");
        keycloakUser1.setAttributes(attributes);
        KeycloakUser keycloakUser2 = new KeycloakUser();
        keycloakUser2.setUsername("nikkinicholas.romero@gmail.com");
        List<KeycloakUser> keycloakUserList = new ArrayList<>();
        keycloakUserList.add(keycloakUser1);
        keycloakUserList.add(keycloakUser2);
        when(keycloakUserClient.getUserListByUsername(anyString())).thenReturn(keycloakUserList);

        KeycloakException e = assertThrows(KeycloakException.class, () ->
                target.sendAccountActivationCode("nikkinicholas.romero@gmail.com"));
        assertThat(e.getMessage()).isEqualTo(NON_UNIQUE_USERNAME_FOUND_ERROR_MESSAGE);
        verify(keycloakUserClient).getUserListByUsername("nikkinicholas.romero@gmail.com");
        verify(keycloakUserClient, never()).updateKeycloakAccountAttribute(anyString(), any());
        verifyNoInteractions(mailClient);
    }

    @Test
    public void sendAccountActivationCode_whenUserIsAlreadyActivated() {
        Map<String, List<String>> attributes = new HashMap<>();
        attributes.put(ACCOUNT_ACTIVATION_CODE, Collections.singletonList("abcdef"));

        KeycloakUser keycloakUser1 = new KeycloakUser();
        keycloakUser1.setId("someId");
        keycloakUser1.setUsername("nikkinicholas.romero@gmail.com");
        keycloakUser1.setAttributes(attributes);
        keycloakUser1.setEmailVerified(true);
        KeycloakUser keycloakUser2 = new KeycloakUser();
        keycloakUser2.setUsername("sayin.leslieanne@gmail.com");
        List<KeycloakUser> keycloakUserList = new ArrayList<>();
        keycloakUserList.add(keycloakUser1);
        keycloakUserList.add(keycloakUser2);
        when(keycloakUserClient.getUserListByUsername(anyString())).thenReturn(keycloakUserList);

        RequestException e = assertThrows(RequestException.class, () ->
                target.sendAccountActivationCode("nikkinicholas.romero@gmail.com"));
        assertThat(e.getMessage()).isEqualTo(ACCOUNT_IS_ALREADY_ACTIVATED_ERROR_MESSAGE);
        verify(keycloakUserClient).getUserListByUsername("nikkinicholas.romero@gmail.com");
        verify(keycloakUserClient, never()).updateKeycloakAccountAttribute(anyString(), any());
        verifyNoInteractions(mailClient);
    }

    @Test
    public void sendAccountActivationCode() {
        Map<String, List<String>> attributes = new HashMap<>();
        attributes.put(ACCOUNT_ACTIVATION_CODE, Collections.singletonList("abcdef"));

        KeycloakUser keycloakUser1 = new KeycloakUser();
        keycloakUser1.setId("someId");
        keycloakUser1.setUsername("nikkinicholas.romero@gmail.com");
        keycloakUser1.setAttributes(attributes);
        KeycloakUser keycloakUser2 = new KeycloakUser();
        keycloakUser2.setUsername("sayin.leslieanne@gmail.com");
        List<KeycloakUser> keycloakUserList = new ArrayList<>();
        keycloakUserList.add(keycloakUser1);
        keycloakUserList.add(keycloakUser2);
        when(keycloakUserClient.getUserListByUsername(anyString())).thenReturn(keycloakUserList);

        target.sendAccountActivationCode("nikkinicholas.romero@gmail.com");

        verify(keycloakUserClient).getUserListByUsername("nikkinicholas.romero@gmail.com");
        verify(keycloakUserClient).updateKeycloakAccountAttribute(eq("someId"), keycloakAccountAttributeUpdateRequestArgumentCaptor.capture());
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
        assertThat(mail.getBody()).isEqualTo(ACCOUNT_ACTIVATION_EMAIL_BODY);
        assertThat(mail.getTemplate()).isEqualTo(ACCOUNT_ACTIVATION_EMAIL_TEMPLATE);
        assertThat(mail.getTemplateVariables()).isNotNull();
        assertThat(mail.getTemplateVariables().getAccountActivationLink()).isNotEqualTo("http://localhost:4200/accountActivation?emailAddress=nikkinicholas.romero@gmail.com&accountActivationCode=abcdef");
        assertThat(mail.getTemplateVariables().getAccountActivationLink()).startsWith("http://localhost:4200/accountActivation?emailAddress=nikkinicholas.romero@gmail.com&accountActivationCode=");
    }
}
