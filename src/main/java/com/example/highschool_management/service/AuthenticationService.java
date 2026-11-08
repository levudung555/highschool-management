package com.example.highschool_management.service;

import com.example.highschool_management.dto.request.AuthenticationRequest;
import com.example.highschool_management.dto.request.IntrospectRequest;
import com.example.highschool_management.dto.request.LogoutRequest;
import com.example.highschool_management.dto.request.RefreshRequest;
import com.example.highschool_management.dto.response.AuthenticationResponse;
import com.example.highschool_management.dto.response.IntrospectResponse;
import com.nimbusds.jose.JOSEException;
import java.text.ParseException;

public interface AuthenticationService {
    AuthenticationResponse authenticate(AuthenticationRequest request);
    AuthenticationResponse refreshToken(RefreshRequest refreshRequest) throws ParseException, JOSEException;
    IntrospectResponse introspect(IntrospectRequest request);
    void logout(LogoutRequest logoutRequest);
}
