package com.demo.account.client.impl;

import com.demo.account.client.MailClient;
import com.demo.account.exception.MailException;
import com.demo.account.model.Mail;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@Profile("!mock")
public class MailClientImpl implements MailClient {
    private static final String ERROR_MESSAGE = "Could not send email";

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
                .onStatus(HttpStatus::isError, response -> Mono.error(new MailException(ERROR_MESSAGE)))
                .bodyToMono(Void.class)
                .doOnError(e -> {
                    throw new MailException(e.getMessage());
                })
                .block();
    }
}
