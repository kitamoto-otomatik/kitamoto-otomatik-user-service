package com.demo.account.client.fake;

import com.demo.account.client.MailClient;
import com.demo.account.model.Mail;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("mock")
public class FakeMailClient implements MailClient {
    @Override
    public void sendEmail(Mail mail) {
    }
}
