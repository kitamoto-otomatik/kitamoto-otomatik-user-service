package com.demo.token.model;

import lombok.Data;
import lombok.ToString;

import javax.validation.constraints.NotBlank;

@Data
public class GetTokenRequest {
    @NotBlank
    private String username;

    @ToString.Exclude
    @NotBlank
    private String password;
}
