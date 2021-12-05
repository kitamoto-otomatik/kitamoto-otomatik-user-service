package com.demo.account.service;

import com.demo.account.client.KeycloakUserClient;
import com.demo.account.client.MailClient;
import com.demo.account.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CreateAccountService {
    private static final String VERIFICATION_CODE = "verificationCode";
    private static final String PASSWORD = "password";
    private final KeycloakUserClient keycloakUserClient;
    private final MailClient mailClient;

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
    public CreateAccountService(KeycloakUserClient keycloakUserClient, MailClient mailClient) {
        this.keycloakUserClient = keycloakUserClient;
        this.mailClient = mailClient;
    }

    public void createAccount(CreateAccountRequest createAccountRequest) {
        KeycloakUser keycloakUser = transformToKeycloakUser(createAccountRequest);
        keycloakUserClient.createAccount(keycloakUser);
        sendVerificationEmail(createAccountRequest.getUsername(), keycloakUser.getAttributes().get(VERIFICATION_CODE).get(0));
    }

    private KeycloakUser transformToKeycloakUser(CreateAccountRequest createAccountRequest) {
        Map<String, List<String>> attributes = new HashMap<>();
        attributes.put(VERIFICATION_CODE, Collections.singletonList(generateVerificationCode()));

        Credential credential = new Credential();
        credential.setType(PASSWORD);
        credential.setValue(createAccountRequest.getPassword());
        credential.setTemporary(false);

        KeycloakUser keycloakUser = new KeycloakUser();
        keycloakUser.setUsername(createAccountRequest.getUsername());
        keycloakUser.setEmail(createAccountRequest.getUsername());
        keycloakUser.setFirstName(createAccountRequest.getFirstName());
        keycloakUser.setLastName(createAccountRequest.getLastName());
        keycloakUser.setAttributes(attributes);
        keycloakUser.setCredentials(Collections.singletonList(credential));
        return keycloakUser;
    }

    private String generateVerificationCode() {
        Random random = new Random();
        return String.valueOf(random.nextInt(1_000_000));
    }

    private void sendVerificationEmail(String toEmail, String verificationCode) {
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
}
