package com.demo.account.client.impl;

import com.demo.account.exception.MailException;
import com.demo.account.model.AccountActivationTemplateVariables;
import com.demo.account.model.Mail;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;

import static com.demo.ErrorMessage.EMAIL_SENDING_ERROR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class MailClientImplTest {
    private static final String HOST = "http://localhost";
    private static final String ENDPOINT = "/mail";

    private MockWebServer mockBackEnd;

    private ObjectMapper objectMapper;

    @InjectMocks
    private MailClientImpl<AccountActivationTemplateVariables> target;

    private Mail<AccountActivationTemplateVariables> mail;

    @BeforeEach
    public void setup() throws IOException {
        mockBackEnd = new MockWebServer();
        mockBackEnd.start();

        objectMapper = new ObjectMapper();

        MockitoAnnotations.openMocks(this);

        ReflectionTestUtils.setField(target, "host", HOST + ":" + mockBackEnd.getPort());
        ReflectionTestUtils.setField(target, "endpoint", ENDPOINT);

        AccountActivationTemplateVariables accountActivationTemplateVariables = new AccountActivationTemplateVariables();
        accountActivationTemplateVariables.setAccountActivationLink("Some Account Activation Link");

        mail = new Mail<>();
        mail.setFrom("someFrom@gmail.com");
        mail.setTo("someTo@gmail.com");
        mail.setSubject("Some Subject");
        mail.setBody("Some Body");
        mail.setTemplate("Some Template");
        mail.setTemplateVariables(accountActivationTemplateVariables);
    }

    @AfterEach
    public void tearDown() throws IOException {
        mockBackEnd.shutdown();
    }

    @Test
    public void sendEmail_whenOk() throws InterruptedException, JsonProcessingException {
        mockBackEnd.enqueue(new MockResponse());

        target.sendEmail(mail);

        RecordedRequest recordedRequest = mockBackEnd.takeRequest();
        assertThat(recordedRequest.getMethod()).isEqualTo("PUT");
        assertThat(recordedRequest.getPath()).isEqualTo(ENDPOINT);
        assertThat(recordedRequest.getBody().readUtf8()).isEqualTo(objectMapper.writeValueAsString(mail));
    }

    @Test
    public void sendEmail_whenError() throws InterruptedException, JsonProcessingException {
        MockResponse mockResponse = new MockResponse();
        mockResponse.status("HTTP/1.1 500 Internal Server Error");
        mockBackEnd.enqueue(mockResponse);

        MailException e = assertThrows(MailException.class, () -> target.sendEmail(mail));
        assertThat(e.getMessage()).isEqualTo(EMAIL_SENDING_ERROR);

        RecordedRequest recordedRequest = mockBackEnd.takeRequest();
        assertThat(recordedRequest.getMethod()).isEqualTo("PUT");
        assertThat(recordedRequest.getPath()).isEqualTo(ENDPOINT);
        assertThat(recordedRequest.getBody().readUtf8()).isEqualTo(objectMapper.writeValueAsString(mail));
    }
}
