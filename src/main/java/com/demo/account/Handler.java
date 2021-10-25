package com.demo.account;

import com.demo.account.model.AccountStatusResponse;
import com.demo.account.model.CreateAccountRequest;
import com.demo.account.model.ErrorResponse;
import com.demo.account.model.KeycloakUser;
import com.demo.account.service.CreateAccountService;
import com.demo.account.service.GetAccountStatusByUsernameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public class Handler {
    @Autowired
    private GetAccountStatusByUsernameService getAccountStatusByUsernameService;

    @Autowired
    private CreateAccountService createAccountService;

    public Mono<ServerResponse> getAccountStatusByUsername(ServerRequest request) {
        return getAccountStatusByUsernameService.getAccountStatusByUsername(request.pathVariable("username"))
                .flatMap(e -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(BodyInserters.fromValue(new AccountStatusResponse(e))))
                .onErrorResume(e -> ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(BodyInserters.fromValue(new ErrorResponse(e.getClass().getSimpleName(), e.getMessage())))
                );
    }

    public Mono<ServerResponse> createAccount(ServerRequest request) {
        return createAccountService.createAccount(request.bodyToMono(CreateAccountRequest.class))
                .flatMap(e -> ServerResponse.status(HttpStatus.CREATED).build())
                .onErrorResume(e -> ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(BodyInserters.fromValue(new ErrorResponse(e.getClass().getSimpleName(), e.getMessage())))
                );
    }
}
