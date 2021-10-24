package com.demo.account;

import com.demo.account.model.AccountStatusResponse;
import com.demo.account.model.ErrorBody;
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
    private Service service;

    public Mono<ServerResponse> getAccountStatusByUsername(ServerRequest request) {
        return service.getAccountStatusByUsername(request.pathVariable("username"))
                .flatMap(e ->
                        ServerResponse.ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(BodyInserters.fromValue(new AccountStatusResponse(e))))
                .onErrorResume(e ->
                        ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(BodyInserters.fromValue(new ErrorBody(e.getClass().getSimpleName(), e.getMessage())))
                );
    }
}
