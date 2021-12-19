package com.demo.account.exception;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class RequestException extends RuntimeException {
    private final String message;
}
