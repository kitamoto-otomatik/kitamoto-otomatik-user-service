package com.demo.orchestrator;

import com.demo.model.AuthenticationRequest;
import com.demo.model.AuthenticationResponse;
import com.demo.model.UserAccount;
import com.demo.repository.UserAccountRepository;
import com.demo.service.AuthenticationResponseBuilder;
import com.demo.service.HashService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AuthenticationOrchestrator {
    @Autowired
    private HashService hashService;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private AuthenticationResponseBuilder authenticationResponseBuilder;

    public Optional<AuthenticationResponse> orchestrate(AuthenticationRequest request) {
        Optional<UserAccount> optionalUserAccount = userAccountRepository.getUserAccountByTypeAndEmailAddress(request.getType(), request.getUsername());

        if (optionalUserAccount.isPresent()) {
            UserAccount userAccount = optionalUserAccount.get();
            String hashedPassword = hashService.hash(request.getPassword(), userAccount.getSalt());
            if (hashedPassword.equals(userAccount.getPassword())) {
                return Optional.of(authenticationResponseBuilder.buildAuthenticationResponse(request.getUsername()));
            }
        }

        return Optional.empty();
    }
}
