package com.demo.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class HashServiceTest {
    @InjectMocks
    private HashService target;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void hash_whenValidHashAlgorithm() {
        ReflectionTestUtils.setField(target, "hashAlgorithm", "SHA-256");

        String salt = target.generateRandomSalt();
        String hashed1 = target.hash("someClearText", salt);
        String hashed2 = target.hash("someClearText", salt);
        String hashed3 = target.hash("someClearText", salt + "1");
        String hashed4 = target.hash("someClearText" + "1", salt);

        assertThat(hashed1).isEqualTo(hashed2);
        assertThat(hashed1).isNotEqualTo(hashed3);
        assertThat(hashed1).isNotEqualTo(hashed4);
    }

    @Test
    public void hash_whenInvalidHashAlgorithm() {
        ReflectionTestUtils.setField(target, "hashAlgorithm", "SHA-256sss");

        assertThrows(RuntimeException.class, () -> {
            target.hash("someClearText", "someSalt");
        });
    }
}
