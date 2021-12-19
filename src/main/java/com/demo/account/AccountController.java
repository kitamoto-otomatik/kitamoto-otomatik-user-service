package com.demo.account;

import com.demo.account.exception.KeycloakException;
import com.demo.account.exception.MailException;
import com.demo.account.exception.RequestException;
import com.demo.account.model.AccountStatusResponse;
import com.demo.account.model.CreateAccountRequest;
import com.demo.account.model.ErrorResponse;
import com.demo.account.service.AccountActivationEmailService;
import com.demo.account.service.AccountActivationService;
import com.demo.account.service.CreateAccountService;
import com.demo.account.service.GetAccountStatusByUsernameService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;

import javax.validation.ConstraintViolationException;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.stream.Collectors;

@Slf4j
@Validated
@RestController
@RequestMapping("/accounts")
public class AccountController {
    @Autowired
    private GetAccountStatusByUsernameService getAccountStatusByUsernameService;

    @Autowired
    private CreateAccountService createAccountService;

    @Autowired
    private AccountActivationService accountActivationService;

    @Autowired
    private AccountActivationEmailService activationEmailService;

    @GetMapping("/{username}")
    public AccountStatusResponse getAccountStatusByUsername(@PathVariable String username) {
        log.info("getAccountStatusByUsername {}", username);
        return new AccountStatusResponse(getAccountStatusByUsernameService.getAccountStatusByUsername(username));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void createAccount(@RequestBody @Valid CreateAccountRequest request) {
        log.info("createAccount {}", request);
        createAccountService.createAccount(request);
    }

    @PostMapping("/{username}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void activateAccount(@PathVariable @NotBlank String username,
                                @RequestParam @NotBlank String activationCode) {
        log.info("activateAccount {} - {}", username, activationCode);
        accountActivationService.activateAccount(username, activationCode);
    }

    @PostMapping("/{username}/resendActivationCode")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void resendActivationCode(@PathVariable @NotBlank String username) {
        log.info("resendActivationCode {}", username);
        activationEmailService.resendActivationCode(username);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({
            MissingPathVariableException.class,
            MissingServletRequestParameterException.class,
            ConstraintViolationException.class,
            RequestException.class
    })
    public HttpEntity<ErrorResponse> badRequestExceptionHandler(Exception e) {
        return new HttpEntity<>(new ErrorResponse(e.getClass().getSimpleName(), e.getMessage()));
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public HttpEntity<ErrorResponse> badRequestExceptionHandler(MethodArgumentNotValidException e) {
        return new HttpEntity<>(new ErrorResponse(e.getClass().getSimpleName(), getValidationErrors(e)));
    }

    public String getValidationErrors(MethodArgumentNotValidException e) {
        return e.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + " " + fieldError.getDefaultMessage())
                .sorted()
                .collect(Collectors.joining(", "));

    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler({
            KeycloakException.class,
            MailException.class
    })
    public HttpEntity<ErrorResponse> internalErrorExceptionHandler(Exception e) {
        return new HttpEntity<>(new ErrorResponse(e.getClass().getSimpleName(), e.getMessage()));
    }
}
