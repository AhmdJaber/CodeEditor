package com.example.CodeEditor.controllers.auth;

import com.example.CodeEditor.enums.Role;
import com.example.CodeEditor.model.users.client.Client;
import com.example.CodeEditor.model.users.client.Token;
import com.example.CodeEditor.repository.ClientRepository;
import com.example.CodeEditor.repository.TokenRepository;
import com.example.CodeEditor.security.AuthenticationResponse;
import com.example.CodeEditor.security.jwt.JwtService;
import com.example.CodeEditor.services.EditorService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

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

    @Autowired
    private TokenRepository tokenRepository;

    public AuthenticationResponse register(RegisterRequest request, String role) throws IOException { // TODO: move it to the ClientService
        Client client = Client
                .builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.valueOf(role))
                .build();
        Client savedClient = clientRepository.save(client);
        if (Role.valueOf(role) == Role.EDITOR){
            editorService.addEditor(client);
        }
        String accessToken = jwtService.generateAccessToken(client);
        String refreshToken = jwtService.generateRefreshToken(client);
        saveClientToken(savedClient, accessToken);
        return new AuthenticationResponse(accessToken, refreshToken);
    }

    private void saveClientToken(Client client, String jwtToken) {
        Token token = Token
                .builder()
                .client(client)
                .token(jwtToken)
                .expired(false)
                .revoked(false)
                .build();
        tokenRepository.save(token);
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        Client client = clientRepository.findByEmail(request.getEmail()).orElseThrow();
        String accessToken = jwtService.generateAccessToken(client);
        String refreshToken = jwtService.generateRefreshToken(client);
        deleteAllClientExpiredTokens(client);
        saveClientToken(client, accessToken);
        return new AuthenticationResponse(accessToken, refreshToken);
    }

    public void deleteAllClientExpiredTokens(Client client){
        List<Token> tokens = tokenRepository.findAllValidTokenClient(client.getId());
        if (tokens.isEmpty()){
            return;
        }

//        for (Token token : tokens){
//            token.setExpired(true);
//            token.setRevoked(true);
//            // TODO: delete the expired tokens?
//        }
//        tokenRepository.saveAll(tokens);
        tokenRepository.deleteAll(tokens);
    }

    public AuthenticationResponse refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        final String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return null;
        }

        final String refreshToken = authorizationHeader.substring(7);
        final String email = jwtService.extractUsername(refreshToken);
        if (email != null) {
            Client client = this.clientRepository.findByEmail(email).orElseThrow();
            if (jwtService.isTokenValid(refreshToken, client)) {
                String accessToken = jwtService.generateAccessToken(client);
                deleteAllClientExpiredTokens(client);
                saveClientToken(client, accessToken);
                AuthenticationResponse authResponse = new AuthenticationResponse(accessToken, refreshToken);
                new ObjectMapper().writeValue(response.getOutputStream(), authResponse);
                return authResponse;
            }
        }

        return null;
    }
}
