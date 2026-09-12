package com.syncvault.user_service.security;

import com.syncvault.user_service.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

    private final SecretKey signingKey;
    private final long accessTokenExpiration;


    public JwtService(
            @Value("${application.security.jwt.secret-key}")
            String secretKey,
            @Value("${application.security.jwt.access-expiration}")
            long accessTokenExpiration)
            {
        this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
        this.accessTokenExpiration = accessTokenExpiration;

    }

    public String generateToken(User user){

        return Jwts
                .builder()
                .subject(user.getId().toString())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessTokenExpiration))
                .signWith(signingKey)
                .compact();
    }

    public UUID extractUserId(String token){
        return UUID.fromString(parseClaims(token).getSubject());
    }

    public boolean validateToken(String token){
        try{
            parseClaims(token);
            return true;
        } catch(JwtException | IllegalArgumentException ex){
            return false;
        }
    }

    public long getAccessTokenExpirationSeconds(){
        return accessTokenExpiration / 1000;
    }

    private Claims parseClaims(String token){
        return Jwts
                .parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
