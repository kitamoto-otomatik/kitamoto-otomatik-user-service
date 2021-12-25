package com.demo.account.model;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
public class ResetPasswordRequest {
    @Email
    @NotBlank
    private String username;

    @NotBlank
    private String password;

    @NotBlank
    private String passwordResetCode;
}
