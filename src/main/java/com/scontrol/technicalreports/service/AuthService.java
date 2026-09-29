package com.scontrol.technicalreports.service;

import java.time.Instant;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import com.scontrol.technicalreports.config.JwtProperties;
import com.scontrol.technicalreports.dto.LoginRequest;
import com.scontrol.technicalreports.dto.LoginResponse;

/**
 * Servicio de autenticación: valida credenciales y emite el JWT de acceso.
 *
 * @author Roger Rojas Effio - roger.rojas@rmsolutions.pe
 */
@Service
public class AuthService {

    private static final String ISSUER = "technical-reports";

    private final AuthenticationManager authenticationManager;
    private final JwtEncoder jwtEncoder;
    private final JwtProperties jwtProperties;

    public AuthService(AuthenticationManager authenticationManager, JwtEncoder jwtEncoder,
                       JwtProperties jwtProperties) {
        this.authenticationManager = authenticationManager;
        this.jwtEncoder = jwtEncoder;
        this.jwtProperties = jwtProperties;
    }

    public LoginResponse login(LoginRequest request) {
        // Lanza AuthenticationException si las credenciales no son válidas
        Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(request.email(), request.password()));

        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(ISSUER)
                .subject(authentication.getName())
                .issuedAt(now)
                .expiresAt(now.plus(jwtProperties.expiration()))
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();

        return new LoginResponse(token, "Bearer", jwtProperties.expiration().toSeconds());
    }
}
