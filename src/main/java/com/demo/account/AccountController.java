package com.demo.account;

import com.demo.account.model.*;
import com.demo.account.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

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
    private AccountActivationEmailService accountActivationEmailService;

    @Autowired
    private AccountActivationService accountActivationService;

    @Autowired
    private SendPasswordResetService sendPasswordResetService;

    @Autowired
    private ResetPasswordService resetPasswordService;

    @Autowired
    private UpdateProfileService updateProfileService;

    @Autowired
    private UpdatePasswordService updatePasswordService;

    @GetMapping("/{username}")
    public AccountStatusResponse getAccountStatusByUsername(@PathVariable @Email String username) {
        log.info("getAccountStatusByUsername {}", username);
        return new AccountStatusResponse(getAccountStatusByUsernameService.getAccountStatusByUsername(username));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void createAccount(@RequestBody @Valid CreateAccountRequest request) {
        log.info("createAccount {}", request);
        createAccountService.createAccount(request);
    }

    @PostMapping("/{username}/account-activation-code")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void sendAccountActivationCode(@PathVariable @NotBlank @Email String username) {
        log.info("sendAccountActivationCode {}", username);
        accountActivationEmailService.sendAccountActivationCode(username);
    }

    @PostMapping("/{username}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void activateAccount(@PathVariable @NotBlank String username,
                                @RequestParam @NotBlank String accountActivationCode) {
        log.info("activateAccount {} - {}", username, accountActivationCode);
        accountActivationService.activateAccount(username, accountActivationCode);
    }

    @PostMapping("/{username}/password-reset")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void sendPasswordResetCode(@PathVariable @NotBlank @Email String username) {
        log.info("sendPasswordResetCode {}", username);
        sendPasswordResetService.sendPasswordResetCode(username);
    }

    @PostMapping("/password-reset")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void resetPassword(@RequestBody @Valid ResetPasswordRequest request) {
        log.info("resetPassword {}", request);
        resetPasswordService.resetPassword(request);
    }

    @PostMapping("/profile")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void updateProfile(@RequestHeader String token, @RequestBody @Valid UpdateProfileRequest request) {
        log.info("updateProfile {} - {}", token, request);
        updateProfileService.updateProfile(token, request);
    }

    @PostMapping("/password")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void updatePassword(@RequestHeader String token, @RequestBody @Valid UpdatePasswordRequest request) {
        log.info("updatePassword {} - {}", token, request);
        updatePasswordService.updatePassword(token, request);
    }
}
