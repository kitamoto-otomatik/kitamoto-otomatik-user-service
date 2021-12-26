package com.demo.mail.client.fake;

import com.demo.mail.client.MailClient;
import com.demo.mail.model.Mail;
import com.demo.mail.model.TemplateVariables;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("mock")
public class FakeMailClient<T extends TemplateVariables> implements MailClient<T> {
    @Override
    public void sendEmail(Mail<T> mail) {
    }
}
