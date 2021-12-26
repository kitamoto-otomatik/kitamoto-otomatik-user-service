package com.demo.mail.model;

import lombok.Data;

@Data
public class PasswordResetTemplateVariables implements TemplateVariables {
    private String passwordResetLink;
}
