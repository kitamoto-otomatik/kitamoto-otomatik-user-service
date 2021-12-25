package com.demo.account.model;

import lombok.Data;
import lombok.ToString;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
public class ResetPasswordRequest {
    @Email
    @NotBlank
    private String username;

    @ToString.Exclude
    @NotBlank
    private String password;

    @ToString.Exclude
    @NotBlank
    private String passwordResetCode;
}
