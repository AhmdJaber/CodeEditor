package com.example.CodeEditor.security.config;

import com.example.CodeEditor.security.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {
    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;
    private final LogoutService logoutService;
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(customizer -> customizer.disable())
                .authorizeHttpRequests(request -> request
                        .requestMatchers(
                                "/auth/**"
                        )
                        .permitAll()
                        .anyRequest()
                        .authenticated()
                )
                .cors((cors) -> {
                    CorsConfiguration config = new CorsConfiguration();
                    config.setAllowCredentials(true);
                    config.addAllowedOrigin("http://localhost:5000/");
                    config.addAllowedHeader("*");
                    config.addAllowedMethod("*");
                    cors.configurationSource(request -> config);
                })
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .logout(logout -> logout
                        .logoutUrl("/auth/logout")
                        .addLogoutHandler(logoutService)
                        .logoutSuccessHandler((request, response, authentication) -> {
                            SecurityContextHolder.clearContext();
                        })
                );
        return http.build();
    }
}
