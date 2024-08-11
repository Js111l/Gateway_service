package com.ecom.gateway.config;

import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;

@Component
public class TokenUtil {
    @Value("${jwt.secret}")
    private String secret;
    @Value("${jwt.salt}")
    private String salt;

    private static final Integer ITERATION_COUNT = 10_000;
    private static final Integer KEY_SIZE = 256;

    public String createToken(Map<String, String> claims, String email, String userId) throws NoSuchAlgorithmException, InvalidKeySpecException {
        return Jwts.builder()
                .claims(claims)
                .subject(email)
                .issuedAt(Date.from(Instant.now()))
                .expiration(getTokenExpirationDate(Instant.now()))
                .issuer(userId)
                .signWith(getKey())
                .compact();
    }

    private Key getKey() throws NoSuchAlgorithmException, InvalidKeySpecException {
        PBEKeySpec pbeKeySpec = new PBEKeySpec(secret.toCharArray(), salt.getBytes(), ITERATION_COUNT, KEY_SIZE);
        SecretKey pbeKey = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(pbeKeySpec);
        return new SecretKeySpec(pbeKey.getEncoded(), "HmacSHA256");
    }

    private Date getTokenExpirationDate(Instant now) {
        return Date.from(now.plus(60, ChronoUnit.MINUTES));
    }

    public void verifyToken(String token) throws NoSuchAlgorithmException, InvalidKeySpecException {
        Jwts.parser()
                .verifyWith((SecretKey) getKey())
                .build()
                .parseSignedClaims(token);
    }


    public String getUserNameFromToken(String token) throws NoSuchAlgorithmException, InvalidKeySpecException {
        return Jwts.parser().verifyWith((SecretKey) getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }
}
