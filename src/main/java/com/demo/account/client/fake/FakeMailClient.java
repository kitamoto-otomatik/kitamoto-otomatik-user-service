package com.demo.account.client.fake;

import com.demo.account.client.MailClient;
import com.demo.account.model.mail.Mail;
import com.demo.account.model.mail.TemplateVariables;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("mock")
public class FakeMailClient<T extends TemplateVariables> implements MailClient<T> {
    @Override
    public void sendEmail(Mail<T> mail) {
    }
}
