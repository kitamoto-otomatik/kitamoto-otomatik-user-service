package com.demo.account.model;

import lombok.Data;

@Data
public class CreateAccountRequest {
    private String username;
    private String password;
    private String firstName;
    private String lastName;
}
