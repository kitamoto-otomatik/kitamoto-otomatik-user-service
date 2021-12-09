package com.demo.account.service;

import com.demo.account.client.KeycloakUserClient;
import com.demo.account.client.MailClient;
import com.demo.account.exception.KeycloakException;
import com.demo.account.model.KeycloakUser;
import com.demo.account.model.Mail;
import com.demo.account.model.TemplateVariables;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AccountActivationEmailService {
    private static final String MULTIPLE_MATCHING_USERNAME_FOUND = "Multiple matching username found";
    private static final String ACCOUNT_IS_ALREADY_ACTIVATED = "Account is already activated";
    private static final String VERIFICATION_CODE = "verificationCode";
    private final MailClient mailClient;
    private final KeycloakUserClient keycloakUserClient;

    @Value("${account.activation.url}")
    private String accountActivationUrl;

    @Value("${account.activation.email.sender}")
    private String accountActivationEmailSender;

    @Value("${account.activation.email.subject}")
    private String accountActivationEmailSubject;

    @Value("${account.activation.email.body}")
    private String accountActivationEmailBody;

    @Value("${account.activation.email.template}")
    private String accountActivationEmailTemplate;

    @Autowired
    public AccountActivationEmailService(MailClient mailClient, KeycloakUserClient keycloakUserClient) {
        this.mailClient = mailClient;
        this.keycloakUserClient = keycloakUserClient;
    }

    public void sendVerificationEmail(String toEmail, String verificationCode) {
        String accountActivationLink = String.format(accountActivationUrl, toEmail, verificationCode);

        TemplateVariables templateVariables = new TemplateVariables();
        templateVariables.setAccountActivationLink(accountActivationLink);

        Mail mail = new Mail();
        mail.setFrom(accountActivationEmailSender);
        mail.setTo(toEmail);
        mail.setSubject(accountActivationEmailSubject);
        mail.setBody(accountActivationEmailBody);
        mail.setTemplate(accountActivationEmailTemplate);
        mail.setTemplateVariables(templateVariables);

        mailClient.sendEmail(mail);
    }

    public void sendVerificationEmail(String toEmail) {
        List<KeycloakUser> keycloakUserList = keycloakUserClient.getUserListByUsername(toEmail);
        keycloakUserList.removeIf(account -> !toEmail.equals(account.getUsername()));

        if (keycloakUserList.size() != 1) {
            throw new KeycloakException(MULTIPLE_MATCHING_USERNAME_FOUND);
        }

        KeycloakUser keycloakUser = keycloakUserList.get(0);
        if (keycloakUser.isEmailVerified()) {
            throw new KeycloakException(ACCOUNT_IS_ALREADY_ACTIVATED);
        }

        sendVerificationEmail(toEmail, keycloakUserList.get(0).getAttributes().get(VERIFICATION_CODE).get(0));
    }
}
