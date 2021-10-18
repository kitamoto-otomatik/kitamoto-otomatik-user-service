package com.demo.controller;

import com.demo.model.AccountStatusResponse;
import com.demo.orchestrator.AccountStatusOrchestrator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@CrossOrigin
@RestController
@RequestMapping("/accounts")
public class AccountController {
    @Autowired
    private AccountStatusOrchestrator orchestrator;

    @GetMapping("/{username}")
    public AccountStatusResponse getAccountStatus(@PathVariable String username) {
        return new AccountStatusResponse(orchestrator.orchestrate(username));
    }
}
