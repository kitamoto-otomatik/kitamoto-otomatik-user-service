package com.demo.account;

import com.demo.account.exception.KeycloakException;
import com.demo.account.model.AccountStatusResponse;
import com.demo.account.model.CreateAccountRequest;
import com.demo.account.model.ErrorResponse;
import com.demo.account.service.AccountActivationEmailService;
import com.demo.account.service.AccountActivationService;
import com.demo.account.service.CreateAccountService;
import com.demo.account.service.GetAccountStatusByUsernameService;
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
    private AccountActivationEmailService accountActivationEmailService;

    @GetMapping("/{username}")
    public AccountStatusResponse getAccountStatusByUsername(@PathVariable String username) {
        return new AccountStatusResponse(getAccountStatusByUsernameService.getAccountStatusByUsername(username));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void createAccount(@RequestBody @Valid CreateAccountRequest request) {
        createAccountService.createAccount(request);
    }

    @PostMapping("/{emailAddress}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void activateAccount(@PathVariable @NotBlank String emailAddress,
                                @RequestParam @NotBlank String activationCode) {
        accountActivationService.activateAccount(emailAddress, activationCode);
    }

    @PostMapping("/{username}/emailVerification")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void resendEmailVerification(@PathVariable @NotBlank String username) {
        accountActivationEmailService.sendVerificationEmail(username);
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler({
            KeycloakException.class,
            MissingPathVariableException.class,
            MissingServletRequestParameterException.class,
            ConstraintViolationException.class
    })
    public HttpEntity<ErrorResponse> exceptionHandler(Exception e) {
        return new HttpEntity<>(new ErrorResponse(e.getClass().getSimpleName(), e.getMessage()));
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public HttpEntity<ErrorResponse> methodArgumentNotValidExceptionHandler(MethodArgumentNotValidException e) {
        return new HttpEntity<>(new ErrorResponse(e.getClass().getSimpleName(), getValidationErrors(e)));
    }

    public String getValidationErrors(MethodArgumentNotValidException e) {
        return e.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + " " + fieldError.getDefaultMessage())
                .sorted()
                .collect(Collectors.joining(", "));

    }
}
