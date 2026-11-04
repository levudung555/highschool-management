package com.example.highschool_management.service;

import com.example.highschool_management.entity.InvalidatedToken;
import com.example.highschool_management.entity.User;
import com.example.highschool_management.exception.AppException;
import com.example.highschool_management.exception.ErrorCode;
import com.example.highschool_management.repository.InvalidatedTokenRepository;
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
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
@Slf4j
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class JwtTokenService {
  @Value("${jwt.signer-key:${jwt.signerKey:1234567890123456789012345678901234567890123456789012345678901234}}")
  @NonFinal
  protected String SIGNER_KEY;

  @Value("${jwt.valid-duration:3600}")
  @NonFinal
  protected Long VALID_DURATION;

  @Value("${jwt.refreshable-duration:7200}")
  @NonFinal
  protected Long REFRESHABLE_DURATION;

  @NonFinal
  @Value("${jwt.issuer:school-management}")
  protected String ISSUER;

  InvalidatedTokenRepository invalidatedTokenRepository;

  public String generateAuthToken(User user, Long durationSeconds) {
    JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);
    JWTClaimsSet claims =
        new JWTClaimsSet.Builder()
            .subject(user.getUsername())
            .issuer(ISSUER)
            .issueTime(new Date())
            .expirationTime(
                new Date(Instant.now().plus(durationSeconds, ChronoUnit.SECONDS).toEpochMilli()))
            .claim("scope", buildScope(user))
            .jwtID(UUID.randomUUID().toString())
            .build();
    Payload payload = new Payload(claims.toJSONObject());
    JWSObject jwsObject = new JWSObject(header, payload);
    try {
      jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
      return jwsObject.serialize();
    } catch (Exception e) {
      log.error("Cannot sign JWT!", e);
      throw new RuntimeException("Error occurred while signing JWT", e);
    }
  }

  public SignedJWT verifierToken(String token, boolean isRefresh)
      throws ParseException, JOSEException {
    JWSVerifier verifier = new MACVerifier(SIGNER_KEY);
    SignedJWT signedJWT = SignedJWT.parse(token);
    if (!JWSAlgorithm.HS512.equals(signedJWT.getHeader().getAlgorithm())) {
      throw new AppException(ErrorCode.AUTHENTICATION_FAILED);
    }
    if (!ISSUER.equals(signedJWT.getJWTClaimsSet().getIssuer())) {
      throw new AppException(ErrorCode.AUTHENTICATION_FAILED);
    }
    var verifiedJWT = signedJWT.verify(verifier);
    Date expiryTime =
        (isRefresh)
            ? new Date(
                signedJWT
                    .getJWTClaimsSet()
                    .getExpirationTime()
                    .toInstant()
                    .plus(REFRESHABLE_DURATION, ChronoUnit.SECONDS)
                    .toEpochMilli())
            : signedJWT.getJWTClaimsSet().getExpirationTime();
    if (!(verifiedJWT && expiryTime != null && expiryTime.after(new Date()))) {
      throw new AppException(ErrorCode.AUTHENTICATION_FAILED);
    }
    if (invalidatedTokenRepository.existsById(signedJWT.getJWTClaimsSet().getJWTID())) {
      throw new AppException(ErrorCode.AUTHENTICATION_FAILED);
    }
    return signedJWT;
  }

  public void revokeToken(String token) throws ParseException, JOSEException {
    var signedToken = verifierToken(token, false);

    String jti = signedToken.getJWTClaimsSet().getJWTID();
    Date expiryTime = signedToken.getJWTClaimsSet().getExpirationTime();

    InvalidatedToken invalidatedToken =
        InvalidatedToken.builder().id(jti).expireTime(expiryTime).build();
    invalidatedTokenRepository.save(invalidatedToken);
  }

  private String buildScope(User user) {
    StringJoiner scope = new StringJoiner(" ");
    if (!CollectionUtils.isEmpty(user.getRoles())) {
      user.getRoles().forEach(role -> {
        scope.add("ROLE_" + role.getName());
      });
    }
    return scope.toString();
  }
}
