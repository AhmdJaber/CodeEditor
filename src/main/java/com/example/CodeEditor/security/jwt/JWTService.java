package com.example.CodeEditor.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JWTService {
    private String key = "";

    public JWTService() throws NoSuchAlgorithmException {
        SecretKey secretKey = KeyGenerator.getInstance("HmacSHA256").generateKey();
        key = Base64.getEncoder().encodeToString(secretKey.getEncoded());
    }

    public String getToken(String username, String role) {
        Map<String, Object> claims = new HashMap<>();

        return Jwts.builder()
                .claims()
                .add(claims)
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 30))
                .and()
                .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(key)))
                .compact();
    }

    public SecretKey getKey(){
        byte[] keyBytes = Decoders.BASE64.decode(key);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String getName(String token){
        return getClaim(token, Claims::getSubject);
    }

    public <T> T getClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public Claims getAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build().parseSignedClaims(token)
                .getPayload();
    }

    public boolean validate(String token, UserDetails details) {
        final String username = getName(token);
        return (username.equals(details.getUsername()) && !isTokenExpired(token));
    }

    public boolean isTokenExpired(String token) {
        return getExpiration(token).before(new Date());
    }

    public Date getExpiration(String token) {
        System.out.println("token: " + token);
        return getClaim(token, Claims::getExpiration);
    }

}
