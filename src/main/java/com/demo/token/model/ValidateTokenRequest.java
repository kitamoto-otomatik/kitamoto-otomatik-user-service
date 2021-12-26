package com.demo.token.model;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class ValidateTokenRequest {
    @NotBlank
    private String token;
}
