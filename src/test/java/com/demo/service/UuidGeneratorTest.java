package com.demo.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;

public class UuidGeneratorTest {
    @InjectMocks
    private UuidGenerator target;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void generateRandomUuid() {
        String uuid1 = target.generateRandomUuid();
        String uuid2 = target.generateRandomUuid();
        assertThat(uuid1).isNotEqualTo(uuid2);
    }
}
