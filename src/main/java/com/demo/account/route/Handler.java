package com.demo.account.route;

import com.demo.account.exception.KeycloakException;
import com.demo.account.model.AccountStatus;
import com.demo.account.model.AccountStatusResponse;
import com.demo.account.service.AccountStatusService;
import com.demo.model.ErrorBody;
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
    private AccountStatusService service;

    public Mono<ServerResponse> getAccountStatusByUsername(ServerRequest request) {
        try {
            Mono<AccountStatus> accountStatus = service.getAccountStatusByUsername(request.pathVariable("username"));

            return ServerResponse.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(accountStatus.map(AccountStatusResponse::new), AccountStatusResponse.class);
        } catch (KeycloakException e) {
            return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(BodyInserters.fromValue(new ErrorBody(e.getClass().getSimpleName(), e.getMessage())));
        }
    }
}
