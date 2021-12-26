package com.demo.account.model.mail;

import lombok.Data;

@Data
public class AccountActivationTemplateVariables implements TemplateVariables {
    private String accountActivationLink;
}
