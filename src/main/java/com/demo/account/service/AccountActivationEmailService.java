package com.demo.account.service;

import com.demo.account.exception.RequestException;
import com.demo.keycloak.client.KeycloakUserClient;
import com.demo.keycloak.model.KeycloakAccountAttributeUpdateRequest;
import com.demo.keycloak.model.KeycloakUser;
import com.demo.mail.client.MailClient;
import com.demo.mail.model.AccountActivationTemplateVariables;
import com.demo.mail.model.Mail;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.demo.ErrorMessage.ACCOUNT_IS_ALREADY_ACTIVATED_ERROR_MESSAGE;
import static com.demo.ErrorMessage.USERNAME_DOES_NOT_EXIST_ERROR_MESSAGE;

@Slf4j
@Service
public class AccountActivationEmailService {
    @Value("${account.activation.url}")
    private String accountActivationUrl;

    @Value("${account.activation.code}")
    private String accountActivationCode;

    @Value("${account.activation.email.sender}")
    private String accountActivationEmailSender;

    @Value("${account.activation.email.subject}")
    private String accountActivationEmailSubject;

    @Value("${account.activation.email.template}")
    private String accountActivationEmailTemplate;

    @Autowired
    private KeycloakUserClient keycloakUserClient;

    @Autowired
    private MailClient<AccountActivationTemplateVariables> mailClient;

    @Async
    public void sendAccountActivationCode(String username) {
        String accountActivationCode = String.valueOf(new Random().nextInt(1_000_000));
        sendAccountActivationCode(username, accountActivationCode);
    }

    private void sendAccountActivationCode(String username, String accountActivationCode) {
        Optional<KeycloakUser> optionalKeycloakUser = keycloakUserClient.getUserByUsername(username);
        if (optionalKeycloakUser.isEmpty()) {
            throw new RequestException(USERNAME_DOES_NOT_EXIST_ERROR_MESSAGE);
        }

        KeycloakUser keycloakUser = optionalKeycloakUser.get();
        if (keycloakUser.isEmailVerified()) {
            throw new RequestException(ACCOUNT_IS_ALREADY_ACTIVATED_ERROR_MESSAGE);
        }

        updateAccountActivationCode(keycloakUser.getId(), accountActivationCode);
        mailClient.sendEmail(buildMail(username, accountActivationCode));
    }

    private void updateAccountActivationCode(String id, String accountActivationCode) {
        Map<String, List<String>> attributes = new HashMap<>();
        attributes.put(this.accountActivationCode, Collections.singletonList(accountActivationCode));
        KeycloakAccountAttributeUpdateRequest keycloakAccountAttributeUpdateRequest = new KeycloakAccountAttributeUpdateRequest();
        keycloakAccountAttributeUpdateRequest.setAttributes(attributes);
        keycloakUserClient.updateUser(id, keycloakAccountAttributeUpdateRequest);
    }

    private Mail<AccountActivationTemplateVariables> buildMail(String username, String accountActivationCode) {
        String accountActivationLink = String.format(accountActivationUrl, username, accountActivationCode);

        AccountActivationTemplateVariables accountActivationTemplateVariables = new AccountActivationTemplateVariables();
        accountActivationTemplateVariables.setAccountActivationLink(accountActivationLink);

        Mail<AccountActivationTemplateVariables> mail = new Mail<>();
        mail.setFrom(accountActivationEmailSender);
        mail.setTo(username);
        mail.setSubject(accountActivationEmailSubject);
        mail.setTemplate(accountActivationEmailTemplate);
        mail.setTemplateVariables(accountActivationTemplateVariables);
        return mail;
    }
}
