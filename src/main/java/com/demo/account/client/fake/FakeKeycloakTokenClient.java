package com.demo.account.client.fake;

import com.demo.account.client.KeycloakTokenClient;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("mock")
public class FakeKeycloakTokenClient implements KeycloakTokenClient {
    @Override
    public String getKeycloakToken() {
        return "someKeycloakToken";
    }
}
