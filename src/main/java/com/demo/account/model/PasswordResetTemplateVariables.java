package com.demo.account.model;

import lombok.Data;

@Data
public class PasswordResetTemplateVariables implements TemplateVariables {
    private String passwordResetLink;
}
