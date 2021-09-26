package com.demo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

@Service
public class HashService {
    @Value("${hash.algorithm}")
    private String hashAlgorithm;

    public String hash(String clear, String salt) {
        try {
            String text = salt + clear + salt;
            MessageDigest messageDigest = MessageDigest.getInstance(hashAlgorithm);
            messageDigest.update(text.getBytes());
            byte[] byteData = messageDigest.digest();

            StringBuilder stringBuilder = new StringBuilder();
            for (byte byteDatum : byteData) {
                stringBuilder.append(Integer.toString((byteDatum & 0xff) + 0x100, 16).substring(1));
            }

            return stringBuilder.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public String generateRandomSalt() {
        return new BigInteger(130, new SecureRandom()).toString(32);
    }
}
