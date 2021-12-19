package com.demo.account.service;

import com.demo.account.client.KeycloakUserClient;
import com.demo.account.client.MailClient;
import com.demo.account.exception.KeycloakException;
import com.demo.account.exception.RequestException;
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
    private static final String ACTIVATION_CODE = "activationCode";

    private static final String URL = "http://localhost:4200/accountActivation?emailAddress=%s&activationCode=%s";
    private static final String CODE = "activationCode";
    private static final String SENDER = "someSender";
    private static final String SUBJECT = "someSubject";
    private static final String BODY = "someBody";
    private static final String TEMPLATE = "someTemplate";

    @InjectMocks
    private AccountActivationEmailService target;

    @Mock
    private MailClient mailClient;

    @Mock
    private KeycloakUserClient keycloakUserClient;

    @Captor
    private ArgumentCaptor<Mail> mailArgumentCaptor;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        ReflectionTestUtils.setField(target, "url", URL);
        ReflectionTestUtils.setField(target, "code", CODE);
        ReflectionTestUtils.setField(target, "sender", SENDER);
        ReflectionTestUtils.setField(target, "subject", SUBJECT);
        ReflectionTestUtils.setField(target, "body", BODY);
        ReflectionTestUtils.setField(target, "template", TEMPLATE);
    }

    @Test
    public void resendActivationCode_whenUsernameDoesNotExist() {
        Map<String, List<String>> attributes = new HashMap<>();
        attributes.put(ACTIVATION_CODE, Collections.singletonList("1234"));

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

        RequestException e = assertThrows(RequestException.class, () -> {
            target.resendActivationCode("non-existent@gmail.com");
        });
        assertThat(e.getMessage()).isEqualTo(USERNAME_DOES_NOT_EXIST_ERROR_MESSAGE);
        verify(keycloakUserClient).getUserListByUsername("non-existent@gmail.com");
        verifyNoInteractions(mailClient);
    }

    @Test
    public void resendActivationCode_whenMultipleUsersMatchTheUsername() {
        Map<String, List<String>> attributes = new HashMap<>();
        attributes.put(ACTIVATION_CODE, Collections.singletonList("1234"));

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

        KeycloakException e = assertThrows(KeycloakException.class, () -> {
            target.resendActivationCode("nikkinicholas.romero@gmail.com");
        });
        assertThat(e.getMessage()).isEqualTo(NON_UNIQUE_USERNAME_FOUND_ERROR_MESSAGE);
        verify(keycloakUserClient).getUserListByUsername("nikkinicholas.romero@gmail.com");
        verifyNoInteractions(mailClient);
    }

    @Test
    public void resendActivationCode_whenUserIsAlreadyActivated() {
        Map<String, List<String>> attributes = new HashMap<>();
        attributes.put(ACTIVATION_CODE, Collections.singletonList("1234"));

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

        RequestException e = assertThrows(RequestException.class, () -> {
            target.resendActivationCode("nikkinicholas.romero@gmail.com");
        });
        assertThat(e.getMessage()).isEqualTo(ACCOUNT_IS_ALREADY_ACTIVATED_ERROR_MESSAGE);
        verify(keycloakUserClient).getUserListByUsername("nikkinicholas.romero@gmail.com");
        verifyNoInteractions(mailClient);
    }

    @Test
    public void resendActivationCode_whenUserIsAlreadyOk() {
        Map<String, List<String>> attributes = new HashMap<>();
        attributes.put(ACTIVATION_CODE, Collections.singletonList("1234"));

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

        target.resendActivationCode("nikkinicholas.romero@gmail.com");

        verify(keycloakUserClient).getUserListByUsername("nikkinicholas.romero@gmail.com");
        verify(mailClient).sendEmail(mailArgumentCaptor.capture());
        Mail mail = mailArgumentCaptor.getValue();
        assertThat(mail).isNotNull();
        assertThat(mail.getFrom()).isEqualTo(SENDER);
        assertThat(mail.getTo()).isEqualTo("nikkinicholas.romero@gmail.com");
        assertThat(mail.getSubject()).isEqualTo(SUBJECT);
        assertThat(mail.getBody()).isEqualTo(BODY);
        assertThat(mail.getTemplate()).isEqualTo(TEMPLATE);
        assertThat(mail.getTemplateVariables()).isNotNull();
        assertThat(mail.getTemplateVariables().getAccountActivationLink()).isEqualTo("http://localhost:4200/accountActivation?emailAddress=nikkinicholas.romero@gmail.com&activationCode=1234");
    }

    @Test
    public void sendActivationCode() {
        target.sendActivationCode("nikkinicholas.romero@gmail.com", "5555");

        verifyNoInteractions(keycloakUserClient);
        verify(mailClient).sendEmail(mailArgumentCaptor.capture());
        Mail mail = mailArgumentCaptor.getValue();
        assertThat(mail).isNotNull();
        assertThat(mail.getFrom()).isEqualTo(SENDER);
        assertThat(mail.getTo()).isEqualTo("nikkinicholas.romero@gmail.com");
        assertThat(mail.getSubject()).isEqualTo(SUBJECT);
        assertThat(mail.getBody()).isEqualTo(BODY);
        assertThat(mail.getTemplate()).isEqualTo(TEMPLATE);
        assertThat(mail.getTemplateVariables()).isNotNull();
        assertThat(mail.getTemplateVariables().getAccountActivationLink()).isEqualTo("http://localhost:4200/accountActivation?emailAddress=nikkinicholas.romero@gmail.com&activationCode=5555");
    }
}
