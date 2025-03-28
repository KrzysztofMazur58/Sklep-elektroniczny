package com.example.sklepElektroniczny.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenUtil {
    private static final Logger logJwt = LoggerFactory.getLogger(JwtTokenUtil.class);

    @Value("${spring.app.jwtSecret}")
    private String secretKey;

    @Value("${spring.app.jwtExpirationMs}")
    private int tokenExpirationMs;

    public String extractJwtFromHeader(HttpServletRequest httpRequest) {
        String authHeader = httpRequest.getHeader("Authorization");
        logJwt.debug("Authorization Header: {}", authHeader);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7); // Remove Bearer prefix
        }
        return null;
    }

    public String createTokenFromUser(UserDetails user) {
        String username = user.getUsername();
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date((new Date()).getTime() + tokenExpirationMs))
                .signWith(generateKey())
                .compact();
    }

    public String extractUsernameFromToken(String jwtToken) {
        return Jwts.parser()
                .verifyWith((SecretKey) generateKey())
                .build().parseSignedClaims(jwtToken)
                .getPayload().getSubject();
    }

    private Key generateKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
    }

    public boolean isTokenValid(String token) {
        try {
            System.out.println("Validating token");
            Jwts.parser().verifyWith((SecretKey) generateKey()).build().parseSignedClaims(token);
            return true;
        } catch (MalformedJwtException e) {
            logJwt.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logJwt.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logJwt.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logJwt.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }
}
