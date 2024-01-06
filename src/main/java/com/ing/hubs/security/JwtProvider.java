package com.ing.hubs.security;

import com.ing.hubs.model.entity.user.CustomUserDetails;
import com.ing.hubs.model.entity.user.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;

import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Data
public class JwtProvider {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.ttlInMinutes}")
    private int ttlInMinutes;

    private static final String ID = "id";
    private static final String ROLES = "roles";


    public String generateJwt(final CustomUserDetails userDetails) {
        final Date expirationDateTime = Date.from(ZonedDateTime.now().plusMinutes(ttlInMinutes).toInstant());

        final Set<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).collect(Collectors.toSet());
        return Jwts
                .builder()
                .subject(userDetails.getUsername())
                .claim(ROLES, roles)
                .claim(ID,userDetails.getId())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(expirationDateTime)
                .signWith(this.getJwtKey())
                .compact();
    }


    public SecretKey getJwtKey() {
        final byte[] keyBytes = Decoders.BASE64.decode(this.secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }


    public String extractUsername(final String token) {
        final Claims claims = Jwts.parser()
                .verifyWith(this.getJwtKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.getSubject();
    }
}
