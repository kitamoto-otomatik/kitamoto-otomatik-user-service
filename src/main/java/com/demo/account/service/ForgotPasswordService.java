package com.demo.account.service;

import com.demo.account.client.KeycloakUserClient;
import com.demo.account.client.MailClient;
import com.demo.account.exception.KeycloakException;
import com.demo.account.exception.RequestException;
import com.demo.account.model.KeycloakAccountAttributeUpdateRequest;
import com.demo.account.model.KeycloakUser;
import com.demo.account.model.Mail;
import com.demo.account.model.PasswordResetTemplateVariables;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.demo.ErrorMessage.*;

@Slf4j
@Service
public class ForgotPasswordService {
    @Value("${password.reset.url}")
    private String url;

    @Value("${password.reset.code}")
    private String code;

    @Value("${password.reset.email.sender}")
    private String sender;

    @Value("${password.reset.email.subject}")
    private String subject;

    @Value("${password.reset.email.body}")
    private String body;

    @Value("${password.reset.email.template}")
    private String template;

    @Autowired
    private KeycloakUserClient keycloakUserClient;

    @Autowired
    private MailClient<PasswordResetTemplateVariables> mailClient;

    public void forgotPassword(String username) {
        List<KeycloakUser> keycloakUserList = keycloakUserClient.getUserListByUsername(username);
        keycloakUserList.removeIf(account -> !username.equals(account.getUsername()));

        if (CollectionUtils.isEmpty(keycloakUserList)) {
            throw new RequestException(USERNAME_DOES_NOT_EXIST_ERROR_MESSAGE);
        } else if (keycloakUserList.size() != 1) {
            log.error("{} - {}", NON_UNIQUE_USERNAME_FOUND_ERROR_MESSAGE, username);
            throw new KeycloakException(NON_UNIQUE_USERNAME_FOUND_ERROR_MESSAGE);
        } else if (!keycloakUserList.get(0).isEmailVerified()) {
            throw new RequestException(ACCOUNT_IS_NOT_YET_ACTIVATED_ERROR_MESSAGE);
        } else {
            String passwordResetCode = UUID.randomUUID().toString();
            resetPasswordResetCode(keycloakUserList.get(0).getId(), passwordResetCode);
            mailClient.sendEmail(buildMail(username, passwordResetCode));
        }
    }

    private void resetPasswordResetCode(String id, String passwordResetCode) {
        Map<String, List<String>> attributes = new HashMap<>();
        attributes.put(code, Collections.singletonList(passwordResetCode));
        KeycloakAccountAttributeUpdateRequest keycloakAccountAttributeUpdateRequest = new KeycloakAccountAttributeUpdateRequest();
        keycloakAccountAttributeUpdateRequest.setAttributes(attributes);
        keycloakUserClient.updateKeycloakAccountAttribute(id, keycloakAccountAttributeUpdateRequest);
    }

    private Mail<PasswordResetTemplateVariables> buildMail(String username, String passwordResetCode) {
        String passwordResetLink = String.format(url, username, passwordResetCode);

        PasswordResetTemplateVariables passwordResetTemplateVariables = new PasswordResetTemplateVariables();
        passwordResetTemplateVariables.setPasswordResetLink(passwordResetLink);

        Mail<PasswordResetTemplateVariables> mail = new Mail<>();
        mail.setFrom(sender);
        mail.setTo(username);
        mail.setSubject(subject);
        mail.setBody(body);
        mail.setTemplate(template);
        mail.setTemplateVariables(passwordResetTemplateVariables);
        return mail;
    }
}
