package com.demo.account.service;

import com.demo.account.client.KeycloakUserClient;
import com.demo.account.client.MailClient;
import com.demo.account.exception.KeycloakException;
import com.demo.account.exception.RequestException;
import com.demo.account.model.KeycloakUser;
import com.demo.account.model.Mail;
import com.demo.account.model.AccountActivationTemplateVariables;
import com.demo.account.model.TemplateVariables;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.demo.ErrorMessage.*;

@Slf4j
@Service
public class AccountActivationEmailService {
    @Value("${account.activation.url}")
    private String url;

    @Value("${account.activation.code}")
    private String code;

    @Value("${account.activation.email.sender}")
    private String sender;

    @Value("${account.activation.email.subject}")
    private String subject;

    @Value("${account.activation.email.body}")
    private String body;

    @Value("${account.activation.email.template}")
    private String template;

    @Autowired
    private KeycloakUserClient keycloakUserClient;

    @Autowired
    private MailClient<AccountActivationTemplateVariables> mailClient;

    @Async
    public void resendActivationCode(String username) {
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
            sendActivationCode(username, keycloakUserList.get(0).getAttributes().get(code).get(0));
        }
    }

    @Async
    public void sendActivationCode(String username, String activationCode) {
        Mail<AccountActivationTemplateVariables> mail = buildMail(username, activationCode);
        mailClient.sendEmail(mail);
    }

    private Mail<AccountActivationTemplateVariables> buildMail(String username, String activationCode) {
        String accountActivationLink = String.format(url, username, activationCode);

        AccountActivationTemplateVariables accountActivationTemplateVariables = new AccountActivationTemplateVariables();
        accountActivationTemplateVariables.setAccountActivationLink(accountActivationLink);

        Mail<AccountActivationTemplateVariables> mail = new Mail<>();
        mail.setFrom(sender);
        mail.setTo(username);
        mail.setSubject(subject);
        mail.setBody(body);
        mail.setTemplate(template);
        mail.setTemplateVariables(accountActivationTemplateVariables);
        return mail;
    }
}
