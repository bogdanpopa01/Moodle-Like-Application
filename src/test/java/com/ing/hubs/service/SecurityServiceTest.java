package com.ing.hubs.service;

import com.ing.hubs.dto.jwt.JwtDto;
import com.ing.hubs.dto.user.UserSessionDto;
import com.ing.hubs.exception.security.NoTokenFoundException;
import com.ing.hubs.model.entity.user.Role;
import com.ing.hubs.model.entity.user.User;
import com.ing.hubs.repository.UserRepository;
import com.ing.hubs.security.JwtProvider;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import javax.crypto.SecretKey;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecurityServiceTest {
    @Mock
    private JwtProvider jwtProvider;
    @Mock
    private UserRepository userRepository;
    @Mock
    private AuthenticationManager authenticationManager;
    @InjectMocks
    private SecurityService securityService;

    private final String secret = "MKsSLgwpW/AtoZEeznJMrwAd+Jujrq4Tpjza+7kqBbI=";
    private final SecretKey secretKey = this.createMockedSecretKey();

    private SecretKey createMockedSecretKey() {
        byte[] keyBytes = Decoders.BASE64.decode(this.secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    @Test
    void shouldCallMethodsAndReturnJwtDtoWhenCreatingSession() {
        User user = User.builder()
                .username("username")
                .password("password")
                .role(Role.TEACHER)
                .build();

        final UserSessionDto userSessionDto = new UserSessionDto("username", "password");

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(jwtProvider.generateJwt(user)).thenReturn("generatedJwt");
        final var jwt = this.securityService.createSession(userSessionDto);

        verify(userRepository, times(1)).findByUsername("username");
        verify(authenticationManager, times(1)).authenticate(new UsernamePasswordAuthenticationToken(userSessionDto.getUsername(), userSessionDto.getPassword()));
        verify(jwtProvider, times(1)).generateJwt(user);
        assertEquals(JwtDto.class, jwt.getClass());
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/ExtractIdFromTokenData.csv")
    void shouldExtractUserIdFromJwt(final String jwt,
                                    final String id){
        when(jwtProvider.getJwtKey()).thenReturn(secretKey);
        assertEquals(UUID.fromString(id), this.securityService.extractUserIdFromToken(jwt));
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/ExtractRoleFromTokenData.csv")
    void shouldExtractUserRoleFromJwt(final String jwt,
                                      final String role){
        when(jwtProvider.getJwtKey()).thenReturn(secretKey);
        assertEquals(Role.valueOf(role), this.securityService.extractRoleFromToken(jwt));
    }

    @Test
    void shouldThrowNoTokenFoundExWhenTokenIsEmptyOrNull(){
        assertThrows(NoTokenFoundException.class, () -> this.securityService.checkIfTokenIsEmpty(null));
        assertThrows(NoTokenFoundException.class, () -> this.securityService.checkIfTokenIsEmpty(""));
        assertDoesNotThrow(() -> this.securityService.checkIfTokenIsEmpty("token"));
    }
}