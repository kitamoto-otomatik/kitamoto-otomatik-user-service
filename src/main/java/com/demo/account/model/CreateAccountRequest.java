package com.demo.account.model;

import lombok.Data;
import lombok.ToString;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
public class CreateAccountRequest {
    @Email
    @NotBlank
    private String username;

    @ToString.Exclude
    @NotBlank
    private String password;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;
}
