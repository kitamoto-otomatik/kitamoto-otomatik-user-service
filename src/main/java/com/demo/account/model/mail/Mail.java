package com.demo.account.model.mail;

import lombok.Data;

@Data
public class Mail<T> {
    private String from;
    private String to;
    private String subject;
    private String body;
    private String template;
    private T templateVariables;
}
