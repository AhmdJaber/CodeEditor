package com.example.CodeEditor.controllers.auth;

import com.example.CodeEditor.security.AuthenticationResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {
    @Autowired
    private AuthenticationService authenticationService;

    @PostMapping("/register/{role}")
    public ResponseEntity<AuthenticationResponse> register(@RequestBody RegisterRequest request, @PathVariable String role) throws IOException {
        return ResponseEntity.ok(authenticationService.register(request, role));
    }

    @PostMapping("/authenticate/{role}")
    public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request, @PathVariable String role) throws IOException {
        return ResponseEntity.ok(authenticationService.authenticate(request, role));
    }

    @GetMapping("/refresh-token")
    public ResponseEntity<AuthenticationResponse> refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        return ResponseEntity.ok(authenticationService.refreshToken(request, response));
    }
}

