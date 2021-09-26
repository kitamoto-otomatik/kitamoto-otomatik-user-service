package com.demo.controller;

import com.demo.model.AuthenticationRequest;
import com.demo.model.AuthenticationResponse;
import com.demo.orchestrator.AuthenticationOrchestrator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@CrossOrigin
@RestController
@RequestMapping("/users")
public class UserController {
    @Autowired
    private AuthenticationOrchestrator orchestrator;

    @PostMapping("/auth")
    public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request) {
        return orchestrator.orchestrate(request)
                .map(response -> new ResponseEntity<>(response, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.UNAUTHORIZED));
    }
}
