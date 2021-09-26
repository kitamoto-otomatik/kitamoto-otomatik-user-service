package com.demo.orchestrator;

import com.demo.model.AuthenticationRequest;
import com.demo.model.AuthenticationResponse;
import com.demo.model.UserAccount;
import com.demo.repository.UserAccountRepository;
import com.demo.service.AuthenticationResponseBuilder;
import com.demo.service.HashService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class AuthenticationOrchestratorTest {
    @InjectMocks
    private AuthenticationOrchestrator target;

    @Mock
    private HashService hashService;

    @Mock
    private UserAccountRepository userAccountRepository;

    @Mock
    private AuthenticationResponseBuilder authenticationResponseBuilder;

    private AuthenticationRequest request;
    private UserAccount userAccount;

    @Mock
    private AuthenticationResponse response;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        request = new AuthenticationRequest();
        request.setType("root");
        request.setUsername("some_username");
        request.setPassword("some_password");

        userAccount = new UserAccount();
        userAccount.setPassword("some_hashedPassword");
        userAccount.setSalt("some_salt");
    }

    @Test
    public void orchestrate_whenUserAccountNotFound() {
        when(userAccountRepository.getUserAccountByTypeAndEmailAddress(anyString(), anyString()))
                .thenReturn(Optional.empty());

        Optional<AuthenticationResponse> actual = target.orchestrate(request);

        assertThat(actual).isNotNull();
        assertThat(actual.isPresent()).isFalse();

        verify(userAccountRepository).getUserAccountByTypeAndEmailAddress(request.getType(), request.getUsername());
        verifyNoInteractions(hashService);
        verifyNoInteractions(authenticationResponseBuilder);
    }

    @Test
    public void orchestrate_whenPasswordIsIncorrect() {
        when(userAccountRepository.getUserAccountByTypeAndEmailAddress(anyString(), anyString()))
                .thenReturn(Optional.of(userAccount));
        when(hashService.hash(anyString(), anyString()))
                .thenReturn("some_differentHashedPassword");

        Optional<AuthenticationResponse> actual = target.orchestrate(request);

        assertThat(actual).isNotNull();
        assertThat(actual.isPresent()).isFalse();

        verify(userAccountRepository).getUserAccountByTypeAndEmailAddress(request.getType(), request.getUsername());
        verify(hashService).hash(request.getPassword(), userAccount.getSalt());
        verifyNoInteractions(authenticationResponseBuilder);
    }

    @Test
    public void orchestrate_whenPasswordIsCorrect() {
        when(userAccountRepository.getUserAccountByTypeAndEmailAddress(anyString(), anyString()))
                .thenReturn(Optional.of(userAccount));
        when(hashService.hash(anyString(), anyString()))
                .thenReturn("some_hashedPassword");
        when(authenticationResponseBuilder.buildAuthenticationResponse(anyString()))
                .thenReturn(response);

        Optional<AuthenticationResponse> actual = target.orchestrate(request);

        assertThat(actual).isNotNull();
        assertThat(actual.isPresent()).isTrue();
        assertThat(actual.get()).isEqualTo(response);

        verify(userAccountRepository).getUserAccountByTypeAndEmailAddress(request.getType(), request.getUsername());
        verify(hashService).hash(request.getPassword(), userAccount.getSalt());
        verify(authenticationResponseBuilder).buildAuthenticationResponse(request.getUsername());
    }
}
