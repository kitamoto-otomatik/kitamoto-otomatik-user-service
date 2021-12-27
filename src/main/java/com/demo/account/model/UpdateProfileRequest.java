package com.demo.account.model;

import com.demo.keycloak.model.UserRepresentation;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class UpdateProfileRequest implements UserRepresentation {
    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;
}
