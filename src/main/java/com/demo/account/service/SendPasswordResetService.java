package com.demo.account.service;

import com.demo.account.exception.RequestException;
import com.demo.keycloak.client.KeycloakUserClient;
import com.demo.keycloak.model.KeycloakAccountAttributeUpdateRequest;
import com.demo.keycloak.model.KeycloakUser;
import com.demo.mail.client.MailClient;
import com.demo.mail.model.Mail;
import com.demo.mail.model.PasswordResetTemplateVariables;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.demo.ErrorMessage.ACCOUNT_IS_NOT_YET_ACTIVATED_ERROR_MESSAGE;
import static com.demo.ErrorMessage.USERNAME_DOES_NOT_EXIST_ERROR_MESSAGE;

@Slf4j
@Service
public class SendPasswordResetService {
    @Value("${password.reset.url}")
    private String passwordResetUrl;

    @Value("${password.reset.code}")
    private String passwordResetCode;

    @Value("${password.reset.email.sender}")
    private String passwordResetEmailSender;

    @Value("${password.reset.email.subject}")
    private String passwordResetEmailSubject;

    @Value("${password.reset.email.template}")
    private String passwordResetEmailTemplate;

    @Autowired
    private KeycloakUserClient keycloakUserClient;

    @Autowired
    private MailClient<PasswordResetTemplateVariables> mailClient;

    public void sendPasswordResetCode(String username) {
        Optional<KeycloakUser> optionalKeycloakUser = keycloakUserClient.getUserByUsername(username);
        if (optionalKeycloakUser.isEmpty()) {
            throw new RequestException(USERNAME_DOES_NOT_EXIST_ERROR_MESSAGE);
        }

        KeycloakUser keycloakUser = optionalKeycloakUser.get();
        if (!keycloakUser.isEmailVerified()) {
            throw new RequestException(ACCOUNT_IS_NOT_YET_ACTIVATED_ERROR_MESSAGE);
        }

        String passwordResetCode = UUID.randomUUID().toString();
        updatePasswordResetCode(keycloakUser.getId(), passwordResetCode);
        mailClient.sendEmail(buildMail(username, passwordResetCode));
    }

    private void updatePasswordResetCode(String id, String passwordResetCode) {
        Map<String, List<String>> attributes = new HashMap<>();
        attributes.put(this.passwordResetCode, Collections.singletonList(passwordResetCode));
        KeycloakAccountAttributeUpdateRequest keycloakAccountAttributeUpdateRequest = new KeycloakAccountAttributeUpdateRequest();
        keycloakAccountAttributeUpdateRequest.setAttributes(attributes);
        keycloakUserClient.updateUser(id, keycloakAccountAttributeUpdateRequest);
    }

    private Mail<PasswordResetTemplateVariables> buildMail(String username, String passwordResetCode) {
        String passwordResetLink = String.format(passwordResetUrl, username, passwordResetCode);

        PasswordResetTemplateVariables passwordResetTemplateVariables = new PasswordResetTemplateVariables();
        passwordResetTemplateVariables.setPasswordResetLink(passwordResetLink);

        Mail<PasswordResetTemplateVariables> mail = new Mail<>();
        mail.setFrom(passwordResetEmailSender);
        mail.setTo(username);
        mail.setSubject(passwordResetEmailSubject);
        mail.setTemplate(passwordResetEmailTemplate);
        mail.setTemplateVariables(passwordResetTemplateVariables);
        return mail;
    }
}
