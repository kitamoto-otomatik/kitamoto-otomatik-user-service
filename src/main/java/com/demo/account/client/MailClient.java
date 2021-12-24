package com.demo.account.client;

import com.demo.account.model.Mail;
import com.demo.account.model.TemplateVariables;

public interface MailClient<T extends TemplateVariables> {
    void sendEmail(Mail<T> mail);
}
