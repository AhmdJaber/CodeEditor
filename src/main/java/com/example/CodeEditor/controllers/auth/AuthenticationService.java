package com.example.CodeEditor.controllers.auth;

import com.example.CodeEditor.enums.Role;
import com.example.CodeEditor.model.users.client.Client;
import com.example.CodeEditor.newSecurity.AuthenticationResponse;
import com.example.CodeEditor.newSecurity.jwt.JwtService;
import com.example.CodeEditor.repository.ClientRepository;
import com.example.CodeEditor.services.EditorService;
import com.example.CodeEditor.services.fileSystem.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private EditorService editorService;

    public AuthenticationResponse register(RegisterRequest request, String role) throws IOException { // TODO: move it to the ClientService
        Client client = Client
                .builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.valueOf(role))
                .build();
        clientRepository.save(client);
        if (Role.valueOf(role) == Role.EDITOR){
            editorService.addEditor(client);
        }
        String token = jwtService.generateToken(client);
        return new AuthenticationResponse(token);
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        Client client = clientRepository.findByEmail(request.getEmail()).orElseThrow();
        String token = jwtService.generateToken(client);
        return new AuthenticationResponse(token);
    }
}
