package com.demo.account.exception;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class MailException extends RuntimeException {
    private final String message;
}
