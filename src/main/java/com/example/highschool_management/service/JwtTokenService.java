package com.example.highschool_management.service;

import com.example.highschool_management.model.User;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.SignedJWT;
import java.text.ParseException;

public interface JwtTokenService {
    String generateAuthToken(User user, Long durationSeconds);
    SignedJWT verifierToken(String token, boolean isRefresh) throws ParseException, JOSEException;
    void revokeToken(String token) throws ParseException, JOSEException;
    void revokeToken(SignedJWT signedJWT) throws ParseException;
}
