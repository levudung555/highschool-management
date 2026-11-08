package com.example.highschool_management.service.impl;

import com.example.highschool_management.dao.UserDao;
import com.example.highschool_management.dto.request.AuthenticationRequest;
import com.example.highschool_management.dto.request.IntrospectRequest;
import com.example.highschool_management.dto.request.LogoutRequest;
import com.example.highschool_management.dto.request.RefreshRequest;
import com.example.highschool_management.dto.response.AuthenticationResponse;
import com.example.highschool_management.dto.response.IntrospectResponse;
import com.example.highschool_management.exception.AppException;
import com.example.highschool_management.exception.ErrorCode;
import com.example.highschool_management.model.User;
import com.example.highschool_management.service.AuthenticationService;
import com.example.highschool_management.service.JwtTokenService;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.SignedJWT;
import java.text.ParseException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Slf4j
public class AuthenticationServiceImpl implements AuthenticationService {

    @NonFinal
    @Value("${jwt.valid-duration}")
    protected Long validDuration;

    UserDao userDao;
    PasswordEncoder passwordEncoder;
    JwtTokenService jwtTokenService;

    @Override
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        User user = userDao.findByUsername(request.username())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        boolean match = passwordEncoder.matches(request.password(), user.getPassword());
        if (!match) {
            throw new AppException(ErrorCode.AUTHENTICATION_FAILED);
        }

        if (!user.isEnabled()) {
            throw new AppException(ErrorCode.ACCOUNT_DISABLED);
        }

        String token = jwtTokenService.generateAuthToken(user, validDuration);
        return AuthenticationResponse.builder()
                .token(token)
                .authenticated(true)
                .build();
    }

    @Override
    public AuthenticationResponse refreshToken(RefreshRequest refreshRequest)
            throws ParseException, JOSEException {
        SignedJWT signToken = jwtTokenService.verifierToken(refreshRequest.token(), true);
        jwtTokenService.revokeToken(signToken);

        String username = signToken.getJWTClaimsSet().getSubject();
        User user = userDao.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if (!user.isEnabled()) {
            throw new AppException(ErrorCode.ACCOUNT_DISABLED);
        }

        String token = jwtTokenService.generateAuthToken(user, validDuration);
        return AuthenticationResponse.builder()
                .token(token)
                .authenticated(true)
                .build();
    }

    @Override
    public IntrospectResponse introspect(IntrospectRequest request) {
        String token = request.token();
        boolean isValid = true;
        try {
            jwtTokenService.verifierToken(token, false);
        } catch (AppException | JOSEException | ParseException e) {
            isValid = false;
        }
        return IntrospectResponse.builder().valid(isValid).build();
    }

    @Override
    public void logout(LogoutRequest logoutRequest) {
        try {
            jwtTokenService.revokeToken(logoutRequest.token());
        } catch (AppException e) {
            log.info("Token already invalid: {}", e.getMessage());
        } catch (JOSEException | ParseException e) {
            log.error("Logout failed: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.AUTHENTICATION_FAILED);
        }
    }
}
