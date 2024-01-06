package com.ing.hubs.service;

import com.ing.hubs.dto.jwt.JwtDto;
import com.ing.hubs.dto.user.UserSessionDto;
import com.ing.hubs.exception.EntityNotFoundException;
import com.ing.hubs.exception.security.NoTokenFoundException;
import com.ing.hubs.model.entity.user.Role;
import com.ing.hubs.model.entity.user.User;
import com.ing.hubs.repository.UserRepository;
import com.ing.hubs.security.JwtProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.UUID;

@Service
@AllArgsConstructor
@Slf4j
public class SecurityService {
    private JwtProvider jwtProvider;
    private UserRepository userRepository;
    private AuthenticationManager authenticationManager;

    public JwtDto createSession(final UserSessionDto dto) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(dto.getUsername(), dto.getPassword()));

        final User user = userRepository.findByUsername(dto.getUsername())
                .orElseThrow(() -> new EntityNotFoundException("User"));

        final String jwt = jwtProvider.generateJwt(user);

        return new JwtDto(jwt);
    }


    public UUID extractUserIdFromToken(final String jwtToken) {
        this.checkIfTokenIsEmpty(jwtToken);

        final String tokenWithoutBearer = jwtToken.replace("Bearer ", "").trim();

        final Claims claims = Jwts.parser()
                .verifyWith(this.jwtProvider.getJwtKey())
                .build()
                .parseSignedClaims(tokenWithoutBearer)
                .getPayload();

        return UUID.fromString(claims.get("id", String.class));
    }

    public Role extractRoleFromToken(final String jwtToken) {
        this.checkIfTokenIsEmpty(jwtToken);

        final String tokenWithoutBearer = jwtToken.replace("Bearer ", "").trim();

        final Claims claims = Jwts.parser()
                .verifyWith(this.jwtProvider.getJwtKey())
                .build()
                .parseSignedClaims(tokenWithoutBearer)
                .getPayload();

        return Role.valueOf(claims.get("roles", ArrayList.class).get(0).toString());
    }

    public void checkIfTokenIsEmpty(String jwtToken){
        if (jwtToken == null || jwtToken.isBlank()) {
            throw new NoTokenFoundException();
        }
    }
}
