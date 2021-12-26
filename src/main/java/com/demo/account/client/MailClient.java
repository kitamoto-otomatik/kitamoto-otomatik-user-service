package com.demo.account.client;

import com.demo.account.model.mail.Mail;
import com.demo.account.model.mail.TemplateVariables;

public interface MailClient<T extends TemplateVariables> {
    void sendEmail(Mail<T> mail);
}
