package com.example.highschool_management.service.impl;

import com.example.highschool_management.dao.InvalidatedTokenDao;
import com.example.highschool_management.exception.AppException;
import com.example.highschool_management.exception.ErrorCode;
import com.example.highschool_management.model.InvalidatedToken;
import com.example.highschool_management.model.User;
import com.example.highschool_management.service.JwtTokenService;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.StringJoiner;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class JwtTokenServiceImpl implements JwtTokenService {

    @Value("${jwt.signer-key}")
    @NonFinal
    protected String signerKey;

    @Value("${jwt.valid-duration}")
    @NonFinal
    protected Long validDuration;

    @Value("${jwt.refreshable-duration}")
    @NonFinal
    protected Long refreshableDuration;

    @Value("${jwt.issuer}")
    @NonFinal
    protected String issuer;

    InvalidatedTokenDao invalidatedTokenDao;

    @Override
    public String generateAuthToken(User user, Long durationSeconds) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(user.getUsername())
                .issuer(issuer)
                .issueTime(new Date())
                .expirationTime(
                        new Date(Instant.now().plus(durationSeconds, ChronoUnit.SECONDS).toEpochMilli()))
                .claim("scope", buildScope(user))
                .jwtID(UUID.randomUUID().toString())
                .build();
        Payload payload = new Payload(claims.toJSONObject());
        JWSObject jwsObject = new JWSObject(header, payload);
        try {
            jwsObject.sign(new MACSigner(signerKey.getBytes()));
            return jwsObject.serialize();
        } catch (Exception e) {
            log.error("Cannot sign JWT!", e);
            throw new RuntimeException("Error occurred while signing JWT", e);
        }
    }

    @Override
    public SignedJWT verifierToken(String token, boolean isRefresh) throws ParseException, JOSEException {
        SignedJWT signedJWT = SignedJWT.parse(token);
        if (!JWSAlgorithm.HS512.equals(signedJWT.getHeader().getAlgorithm())) {
            throw new AppException(ErrorCode.AUTHENTICATION_FAILED);
        }
        if (!issuer.equals(signedJWT.getJWTClaimsSet().getIssuer())) {
            throw new AppException(ErrorCode.AUTHENTICATION_FAILED);
        }
        JWSVerifier verifier = new MACVerifier(signerKey);
        boolean verifiedJWT = signedJWT.verify(verifier);
        Date expiryTime = isRefresh
                ? new Date(signedJWT.getJWTClaimsSet().getExpirationTime().toInstant().plus(refreshableDuration, ChronoUnit.SECONDS).toEpochMilli())
                : signedJWT.getJWTClaimsSet().getExpirationTime();
        if (!(verifiedJWT && expiryTime.after(new Date()))) {
            throw new AppException(ErrorCode.AUTHENTICATION_FAILED);
        }
        if (invalidatedTokenDao.existsById(signedJWT.getJWTClaimsSet().getJWTID())) {
            throw new AppException(ErrorCode.AUTHENTICATION_FAILED);
        }
        return signedJWT;
    }

    @Override
    public void revokeToken(String token) throws ParseException, JOSEException {
        SignedJWT signedToken = verifierToken(token, false);
        revokeToken(signedToken);
    }

    @Override
    public void revokeToken(SignedJWT signedJWT) throws ParseException {
        String jti = signedJWT.getJWTClaimsSet().getJWTID();
        Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        InvalidatedToken invalidatedToken = InvalidatedToken.builder()
                .id(jti)
                .expireTime(expiryTime)
                .build();
        invalidatedTokenDao.save(invalidatedToken);
    }

    private String buildScope(User user) {
        StringJoiner scope = new StringJoiner(" ");
        if (!CollectionUtils.isEmpty(user.getRoles())) {
            user.getRoles().forEach(role -> scope.add("ROLE_" + role.getName()));
        }
        return scope.toString();
    }
}
