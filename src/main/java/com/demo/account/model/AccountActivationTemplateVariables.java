package com.demo.account.model;

import lombok.Data;

@Data
public class AccountActivationTemplateVariables implements TemplateVariables {
    private String accountActivationLink;
}
