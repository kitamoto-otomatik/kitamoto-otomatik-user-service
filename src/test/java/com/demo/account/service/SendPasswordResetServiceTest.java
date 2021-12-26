package com.demo.account.service;

import com.demo.account.client.KeycloakUserClient;
import com.demo.account.client.MailClient;
import com.demo.account.exception.RequestException;
import com.demo.account.model.KeycloakAccountAttributeUpdateRequest;
import com.demo.account.model.KeycloakUser;
import com.demo.account.model.Mail;
import com.demo.account.model.PasswordResetTemplateVariables;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static com.demo.ErrorMessage.ACCOUNT_IS_NOT_YET_ACTIVATED_ERROR_MESSAGE;
import static com.demo.ErrorMessage.USERNAME_DOES_NOT_EXIST_ERROR_MESSAGE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class SendPasswordResetServiceTest {
    private static final String PASSWORD_RESET_URL = "http://localhost:4200/passwordReset?emailAddress=%s&passwordResetCode=%s";
    private static final String PASSWORD_RESET_CODE = "passwordResetCode";
    private static final String PASSWORD_RESET_EMAIL_SENDER = "nikkinicholas.romero@gmail.com";
    private static final String PASSWORD_RESET_EMAIL_SUBJECT = "Password Reset";
    private static final String PASSWORD_RESET_EMAIL_BODY = "Reset your password by clicking the link below.";
    private static final String PASSWORD_RESET_EMAIL_TEMPLATE = "password_reset";

    @InjectMocks
    private SendPasswordResetService target;

    @Mock
    private KeycloakUserClient keycloakUserClient;

    @Mock
    private MailClient<PasswordResetTemplateVariables> mailClient;

    @Captor
    private ArgumentCaptor<KeycloakAccountAttributeUpdateRequest> keycloakAccountAttributeUpdateRequestArgumentCaptor;

    @Captor
    private ArgumentCaptor<Mail<PasswordResetTemplateVariables>> mailArgumentCaptor;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        ReflectionTestUtils.setField(target, "passwordResetUrl", PASSWORD_RESET_URL);
        ReflectionTestUtils.setField(target, "passwordResetCode", PASSWORD_RESET_CODE);
        ReflectionTestUtils.setField(target, "passwordResetEmailSender", PASSWORD_RESET_EMAIL_SENDER);
        ReflectionTestUtils.setField(target, "passwordResetEmailSubject", PASSWORD_RESET_EMAIL_SUBJECT);
        ReflectionTestUtils.setField(target, "passwordResetEmailBody", PASSWORD_RESET_EMAIL_BODY);
        ReflectionTestUtils.setField(target, "passwordResetEmailTemplate", PASSWORD_RESET_EMAIL_TEMPLATE);
    }

    @Test
    public void sendPasswordResetCode_whenUsernameDoesNotExist() {
        when(keycloakUserClient.getUserByUsername(anyString())).thenReturn(Optional.empty());

        RequestException e = assertThrows(RequestException.class, () ->
                target.sendPasswordResetCode("non-existent@gmail.com"));
        assertThat(e.getMessage()).isEqualTo(USERNAME_DOES_NOT_EXIST_ERROR_MESSAGE);

        verify(keycloakUserClient).getUserByUsername("non-existent@gmail.com");
        verify(keycloakUserClient, never()).updateKeycloakAccountAttribute(anyString(), any());
        verifyNoInteractions(mailClient);
    }

    @Test
    public void sendPasswordResetCode_whenUserIsNotYetActivated() {
        KeycloakUser keycloakUser = new KeycloakUser();
        keycloakUser.setId("someId");
        keycloakUser.setUsername("nikkinicholas.romero@gmail.com");
        keycloakUser.setEmailVerified(false);
        when(keycloakUserClient.getUserByUsername(anyString())).thenReturn(Optional.of(keycloakUser));

        RequestException e = assertThrows(RequestException.class, () ->
                target.sendPasswordResetCode("nikkinicholas.romero@gmail.com"));
        assertThat(e.getMessage()).isEqualTo(ACCOUNT_IS_NOT_YET_ACTIVATED_ERROR_MESSAGE);

        verify(keycloakUserClient).getUserByUsername("nikkinicholas.romero@gmail.com");
        verify(keycloakUserClient, never()).updateKeycloakAccountAttribute(anyString(), any());
        verifyNoInteractions(mailClient);
    }

    @Test
    public void sendPasswordResetCode() {
        KeycloakUser keycloakUser = new KeycloakUser();
        keycloakUser.setId("someId");
        keycloakUser.setUsername("nikkinicholas.romero@gmail.com");
        keycloakUser.setEmailVerified(true);
        when(keycloakUserClient.getUserByUsername(anyString())).thenReturn(Optional.of(keycloakUser));

        target.sendPasswordResetCode("nikkinicholas.romero@gmail.com");
        verify(keycloakUserClient).getUserByUsername("nikkinicholas.romero@gmail.com");
        verify(keycloakUserClient).updateKeycloakAccountAttribute(eq("someId"), keycloakAccountAttributeUpdateRequestArgumentCaptor.capture());
        KeycloakAccountAttributeUpdateRequest keycloakAccountAttributeUpdateRequest = keycloakAccountAttributeUpdateRequestArgumentCaptor.getValue();
        assertThat(keycloakAccountAttributeUpdateRequest).isNotNull();
        assertThat(keycloakAccountAttributeUpdateRequest.getAttributes()).isNotEmpty();
        assertThat(keycloakAccountAttributeUpdateRequest.getAttributes().get("passwordResetCode")).isNotNull();
        verify(mailClient).sendEmail(mailArgumentCaptor.capture());
        Mail<PasswordResetTemplateVariables> mail = mailArgumentCaptor.getValue();
        assertThat(mail).isNotNull();
        assertThat(mail.getFrom()).isEqualTo(PASSWORD_RESET_EMAIL_SENDER);
        assertThat(mail.getTo()).isEqualTo("nikkinicholas.romero@gmail.com");
        assertThat(mail.getSubject()).isEqualTo(PASSWORD_RESET_EMAIL_SUBJECT);
        assertThat(mail.getBody()).isEqualTo(PASSWORD_RESET_EMAIL_BODY);
        assertThat(mail.getTemplate()).isEqualTo(PASSWORD_RESET_EMAIL_TEMPLATE);
        assertThat(mail.getTemplateVariables()).isNotNull();
        assertThat(mail.getTemplateVariables().getPasswordResetLink()).startsWith("http://localhost:4200/passwordReset?emailAddress=nikkinicholas.romero@gmail.com&passwordResetCode=");
    }
}
