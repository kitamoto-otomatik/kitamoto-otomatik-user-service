package com.demo.account.service;

import com.demo.account.client.KeycloakUserClient;
import com.demo.account.client.MailClient;
import com.demo.account.exception.KeycloakException;
import com.demo.account.exception.RequestException;
import com.demo.account.model.AccountActivationTemplateVariables;
import com.demo.account.model.KeycloakAccountAttributeUpdateRequest;
import com.demo.account.model.KeycloakUser;
import com.demo.account.model.Mail;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.demo.ErrorMessage.*;

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

    @Value("${account.activation.email.body}")
    private String accountActivationEmailBody;

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
        List<KeycloakUser> keycloakUserList = keycloakUserClient.getUserListByUsername(username);
        keycloakUserList.removeIf(account -> !username.equals(account.getUsername()));

        if (CollectionUtils.isEmpty(keycloakUserList)) {
            throw new RequestException(USERNAME_DOES_NOT_EXIST_ERROR_MESSAGE);
        } else if (keycloakUserList.size() != 1) {
            log.error("{} - {}", NON_UNIQUE_USERNAME_FOUND_ERROR_MESSAGE, username);
            throw new KeycloakException(NON_UNIQUE_USERNAME_FOUND_ERROR_MESSAGE);
        } else if (keycloakUserList.get(0).isEmailVerified()) {
            throw new RequestException(ACCOUNT_IS_ALREADY_ACTIVATED_ERROR_MESSAGE);
        } else {
            updateAccountActivationCode(keycloakUserList.get(0).getId(), accountActivationCode);
            mailClient.sendEmail(buildMail(username, accountActivationCode));
        }
    }

    private void updateAccountActivationCode(String id, String accountActivationCode) {
        Map<String, List<String>> attributes = new HashMap<>();
        attributes.put(this.accountActivationCode, Collections.singletonList(accountActivationCode));
        KeycloakAccountAttributeUpdateRequest keycloakAccountAttributeUpdateRequest = new KeycloakAccountAttributeUpdateRequest();
        keycloakAccountAttributeUpdateRequest.setAttributes(attributes);
        keycloakUserClient.updateKeycloakAccountAttribute(id, keycloakAccountAttributeUpdateRequest);
    }

    private Mail<AccountActivationTemplateVariables> buildMail(String username, String accountActivationCode) {
        String accountActivationLink = String.format(accountActivationUrl, username, accountActivationCode);

        AccountActivationTemplateVariables accountActivationTemplateVariables = new AccountActivationTemplateVariables();
        accountActivationTemplateVariables.setAccountActivationLink(accountActivationLink);

        Mail<AccountActivationTemplateVariables> mail = new Mail<>();
        mail.setFrom(accountActivationEmailSender);
        mail.setTo(username);
        mail.setSubject(accountActivationEmailSubject);
        mail.setBody(accountActivationEmailBody);
        mail.setTemplate(accountActivationEmailTemplate);
        mail.setTemplateVariables(accountActivationTemplateVariables);
        return mail;
    }
}
