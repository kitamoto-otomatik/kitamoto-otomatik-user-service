package com.demo.account.model;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class CreateAccountRequest {
    @NotBlank
    private String username;

    @NotBlank
    private String password;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;
}
