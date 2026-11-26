package com.professorisidro.simpleapi.auth;

import com.professorisidro.simpleapi.auth.dto.LoginRequest;
import com.professorisidro.simpleapi.auth.dto.RefreshRequest;
import com.professorisidro.simpleapi.auth.dto.TokenResponse;
import com.professorisidro.simpleapi.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;

    public AuthService(AuthenticationManager authenticationManager,
                       UserDetailsService userDetailsService,
                       JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
    }

    public TokenResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));

        UserDetails user = userDetailsService.loadUserByUsername(request.username());
        return new TokenResponse(
                jwtService.generateAccessToken(user),
                jwtService.generateRefreshToken(user),
                "Bearer",
                jwtService.getAccessTokenExpirationSeconds());
    }

    public TokenResponse refresh(RefreshRequest request) {
        String token = request.refreshToken();

        String type;
        String username;
        try {
            type = jwtService.extractType(token);
            username = jwtService.extractUsername(token);
        } catch (Exception ex) {
            throw new BadCredentialsException("Refresh token is invalid or expired");
        }

        if (!JwtService.TYPE_REFRESH.equals(type)) {
            throw new BadCredentialsException("Provided token is not a refresh token");
        }

        UserDetails user = userDetailsService.loadUserByUsername(username);
        if (!jwtService.isTokenValid(token, user)) {
            throw new BadCredentialsException("Refresh token is invalid or expired");
        }

        // Issue a fresh access token; the existing refresh token stays valid until it expires.
        return new TokenResponse(
                jwtService.generateAccessToken(user),
                token,
                "Bearer",
                jwtService.getAccessTokenExpirationSeconds());
    }
}
