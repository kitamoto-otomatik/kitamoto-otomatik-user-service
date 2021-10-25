package com.demo.account.model;

import lombok.Data;

@Data
public class CreateAccountRequest {
    private String username;
    private String firstName;
    private String lastName;
}
