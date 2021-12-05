package com.demo.account;

import com.demo.account.model.AccountStatusResponse;
import com.demo.account.model.CreateAccountRequest;
import com.demo.account.service.CreateAccountService;
import com.demo.account.service.GetAccountStatusByUsernameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/accounts")
public class AccountController {
    @Autowired
    private GetAccountStatusByUsernameService getAccountStatusByUsernameService;

    @Autowired
    private CreateAccountService createAccountService;

    @GetMapping("/{username}")
    public AccountStatusResponse getAccountStatusByUsername(@PathVariable String username) {
        return new AccountStatusResponse(getAccountStatusByUsernameService.getAccountStatusByUsername(username));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void createAccount(@RequestBody CreateAccountRequest request) {
        createAccountService.createAccount(request);
    }
}
