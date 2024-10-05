package com.example.CodeEditor.security.config;

import com.example.CodeEditor.controllers.auth.AuthenticationService;
import com.example.CodeEditor.controllers.auth.RegisterRequest;
import com.example.CodeEditor.model.users.client.Client;
import com.example.CodeEditor.repository.ClientRepository;
import com.example.CodeEditor.security.jwt.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {
    private final JwtService jwtService;
    @Autowired
    private ClientRepository clientRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");

        Client client = clientRepository.findByEmail(email).orElseThrow();

        String accessToken = jwtService.generateAccessToken(client);
        String refreshToken = jwtService.generateRefreshToken(client);

        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_OK);


        String redirectUrl = "http://localhost:5000/oauth2/redirect?access_token=" + accessToken + "&refresh_token=" + refreshToken;
        response.sendRedirect(redirectUrl);
    }
}