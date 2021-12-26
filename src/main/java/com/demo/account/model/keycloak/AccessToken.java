package com.demo.account.model.keycloak;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccessToken {
    @JsonProperty("access_token")
    private String accessToken;
}
