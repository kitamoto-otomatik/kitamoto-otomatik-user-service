package com.demo.account.client.impl;

import com.demo.account.client.MailClient;
import com.demo.account.exception.MailException;
import com.demo.account.model.Mail;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import static com.demo.ErrorMessage.EMAIL_SENDING_ERROR;

@Slf4j
@Component
@Profile("!mock")
public class MailClientImpl implements MailClient {
    @Value("${mail.host}")
    private String host;

    @Value("${mail.send.endpoint}")
    private String endpoint;

    @Override
    public void sendEmail(Mail mail) {
        WebClient.builder()
                .baseUrl(host)
                .build()
                .put()
                .uri(e -> e.path(endpoint).build())
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(mail))
                .retrieve()
                .onStatus(HttpStatus::isError, response -> response
                        .createException()
                        .map(e -> new RuntimeException(e.getResponseBodyAsString())))
                .bodyToMono(Void.class)
                .doOnError(e -> {
                    log.error(EMAIL_SENDING_ERROR, e);
                    throw new MailException(EMAIL_SENDING_ERROR);
                })
                .block();
    }
}
