package com.demo.account;

import com.demo.account.exception.KeycloakException;
import com.demo.account.model.AccountStatusResponse;
import com.demo.account.service.AccountStatusService;
import com.demo.model.ErrorBody;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@CrossOrigin
@RestController
@RequestMapping("/accounts")
public class AccountController {
    @Autowired
    private AccountStatusService service;

    @GetMapping("/{username}")
    public AccountStatusResponse getAccountStatus(@PathVariable String username) {
        return new AccountStatusResponse(service.getAccountStatusByUsername(username));
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(KeycloakException.class)
    public ResponseEntity<ErrorBody> handleKeycloakException(KeycloakException e) {
        return ResponseEntity.internalServerError().body(new ErrorBody(e.getClass().getSimpleName(), e.getMessage()));
    }
}
