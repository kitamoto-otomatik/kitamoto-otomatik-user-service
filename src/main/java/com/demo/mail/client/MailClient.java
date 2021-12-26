package com.demo.mail.client;

import com.demo.mail.model.Mail;
import com.demo.mail.model.TemplateVariables;

public interface MailClient<T extends TemplateVariables> {
    void sendEmail(Mail<T> mail);
}
