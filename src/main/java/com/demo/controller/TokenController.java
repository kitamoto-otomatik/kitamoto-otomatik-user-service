package com.demo.controller;

import com.demo.model.TokenDecodeRequest;
import com.demo.model.TokenRefreshRequest;
import com.demo.service.TokenDecodeService;
import com.demo.service.TokenRefreshService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@CrossOrigin
@RestController
@RequestMapping("/tokens")
public class TokenController {
    @Autowired
    private TokenDecodeService tokenDecodeService;

    @Autowired
    private TokenRefreshService tokenRefreshService;

    @PostMapping("/decode")
    public ResponseEntity<?> decode(@RequestBody TokenDecodeRequest request) {
        return tokenDecodeService.decode(request.getToken())
                .map(response -> new ResponseEntity<>(response, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.UNAUTHORIZED));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody TokenRefreshRequest request) {
        return tokenRefreshService.refresh(request.getRefreshToken())
                .map(response -> new ResponseEntity<>(response, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.UNAUTHORIZED));
    }
}
