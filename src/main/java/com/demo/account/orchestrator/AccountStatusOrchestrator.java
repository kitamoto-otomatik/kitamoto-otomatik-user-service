package com.demo.account.orchestrator;

import com.demo.account.service.KeycloakService;
import com.demo.account.model.AccountStatus;
import com.demo.account.model.KeycloakUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AccountStatusOrchestrator {
    @Autowired
    private KeycloakService keycloakService;

    public AccountStatus orchestrate(String username) {
        Optional<KeycloakUser> optionalKeycloakUser = keycloakService.getUserByUsername(username);
        if (optionalKeycloakUser.isPresent()) {
            KeycloakUser keycloakUser = optionalKeycloakUser.get();
            if (keycloakUser.isEmailVerified()) {
                return AccountStatus.ACTIVE;
            } else {
                return AccountStatus.UNVERIFIED;
            }
        } else {
            return AccountStatus.UNREGISTERED;
        }
    }
}
