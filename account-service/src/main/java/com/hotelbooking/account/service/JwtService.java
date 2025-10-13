package com.hotelbooking.account.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {

    @Value("${jwt.private-key}")
    private String privateKeyPem;

    @Value("${jwt.public-key}")
    private String publicKeyPem;

    @Value("${jwt.expiration:3600000}")
    private long expirationMs;

    private PrivateKey getPrivateKey() {
        try {
            String pem = privateKeyPem
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\n", "")
                .trim();
            byte[] keyBytes = Base64.getDecoder().decode(pem);
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePrivate(spec);
        } catch (Exception e) {
            throw new IllegalStateException("Invalid private key", e);
        }
    }

    private PublicKey getPublicKey() {
        try {
            String pem = publicKeyPem
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\n", "")
                .trim();
            byte[] keyBytes = Base64.getDecoder().decode(pem);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePublic(spec);
        } catch (Exception e) {
            throw new IllegalStateException("Invalid public key", e);
        }
    }

    public String generateToken(String subject, Map<String, Object> claims) {
        Instant now = Instant.now();
        Instant exp = now.plusMillis(expirationMs);
        return Jwts.builder()
                .setSubject(subject)
                .addClaims(claims)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(exp))
                .signWith(getPrivateKey(), SignatureAlgorithm.RS256)
                .compact();
    }
}


