package com.demo.account.model.mail;

import lombok.Data;

@Data
public class PasswordResetTemplateVariables implements TemplateVariables {
    private String passwordResetLink;
}
