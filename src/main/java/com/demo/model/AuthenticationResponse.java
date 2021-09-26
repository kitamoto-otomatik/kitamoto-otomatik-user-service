package com.demo.model;

import lombok.Data;

@Data
public class AuthenticationResponse {
    private Token accessToken;
    private Token refreshToken;
}
