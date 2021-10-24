package com.demo.account.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class ErrorBody {
    private final String code;
    private final String message;
}
