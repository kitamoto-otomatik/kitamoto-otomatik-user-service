package com.demo.model;

import lombok.Data;

@Data
public class AuthenticationRequest {
    private String type;
    private String username;
    private String password;
}
