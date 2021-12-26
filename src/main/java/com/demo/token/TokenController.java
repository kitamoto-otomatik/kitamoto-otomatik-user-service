package com.demo.token;

import com.demo.token.model.GetTokenRequest;
import com.demo.token.model.Token;
import com.demo.token.model.ValidateTokenRequest;
import com.demo.token.service.GetTokenService;
import com.demo.token.service.TokenValidationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Slf4j
@Validated
@RestController
@RequestMapping("/token")
public class TokenController {
    @Autowired
    private GetTokenService getTokenService;

    @Autowired
    private TokenValidationService tokenValidationService;

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public Token getToken(@RequestBody @Valid GetTokenRequest request) {
        log.info("getToken {}", request);
        return getTokenService.getToken(request);
    }

    @PostMapping("/validate")
    @ResponseStatus(HttpStatus.OK)
    public void validateToken(@RequestBody @Valid ValidateTokenRequest request) {
        log.info("validateToken {}", request);
        tokenValidationService.validate(request);
    }
}
