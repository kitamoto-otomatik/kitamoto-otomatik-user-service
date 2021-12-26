package com.demo.mail.model;

import lombok.Data;

@Data
public class AccountActivationTemplateVariables implements TemplateVariables {
    private String accountActivationLink;
}
