package at.ac.tuwien.ase.groupphase.backend.security.impl;

import at.ac.tuwien.ase.groupphase.backend.config.properties.SecurityProperties;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
public class JwtTokenizerImpl implements at.ac.tuwien.ase.groupphase.backend.security.JwtTokenizer {

    private final SecurityProperties securityProperties;

    public JwtTokenizerImpl(SecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
    }

    public String getAuthToken(String user, Long id, List<String> roles) {
        byte[] signingKey = securityProperties.getJwtSecret().getBytes();
        SecretKey key = Keys.hmacShaKeyFor(signingKey);

        String token = Jwts.builder()
            .header().add("typ", securityProperties.getJwtType()).and()
            .issuer(securityProperties.getJwtIssuer())
            .audience().add(securityProperties.getJwtAudience()).and()
            .subject(user)
            .expiration(new Date(System.currentTimeMillis() + securityProperties.getJwtExpirationTime()))
            .claim("rol", roles)
            .claim("userId", id)
            .signWith(key, Jwts.SIG.HS512)
            .compact();
        
        return securityProperties.getAuthTokenPrefix() + token;
    }
}
