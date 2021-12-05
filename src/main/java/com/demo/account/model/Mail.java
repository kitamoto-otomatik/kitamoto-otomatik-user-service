package com.demo.account.model;

import lombok.Data;

@Data
public class Mail {
    private String from;
    private String to;
    private String subject;
    private String body;
    private String template;
    private TemplateVariables templateVariables;
}
