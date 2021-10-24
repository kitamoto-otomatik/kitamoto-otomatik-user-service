package com.demo.account.route;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;

@Configuration(proxyBeanMethods = false)
public class Router {
    @Bean
    public RouterFunction<ServerResponse> route(Handler accountHandler) {
        return RouterFunctions.route(GET("/accounts/{username}"), accountHandler::getAccountStatusByUsername);
    }
}
