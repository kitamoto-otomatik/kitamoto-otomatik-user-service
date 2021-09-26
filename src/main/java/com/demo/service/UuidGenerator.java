package com.demo.service;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class UuidGenerator {
    public String generateRandomUuid() {
        return UUID.randomUUID().toString();
    }
}
