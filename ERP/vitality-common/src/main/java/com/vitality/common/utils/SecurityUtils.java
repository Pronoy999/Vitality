package com.vitality.common.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.vitality.common.dtos.JwtData;
import com.vitality.common.dtos.JwtValidationResult;
import com.vitality.common.dtos.TokenData;
import com.vitality.common.exceptions.InvalidTokenException;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

@Slf4j
public final class SecurityUtils {
    @Value("${google.client.id}")
    private String googleClientId;
    @Value("${jwt.secret.key}")
    private String key;

    /**
     * Method to verify the Google OAuth token and extract user information.
     *
     * @param token: the Google token to be verified.
     * @return the user information extracted from the token.
     */
    public TokenData verifyGoogleOAuthToken(@NotNull final String token) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();
            GoogleIdToken googleIdToken = verifier.verify(token);
            if (Objects.nonNull(googleIdToken)) {
                GoogleIdToken.Payload payload = googleIdToken.getPayload();
                if (!payload.getAudience().equals(googleClientId)) {
                    throw new InvalidTokenException("Invalid Audience in Google OAuth Token");
                }
                String firstName = payload.get("given_name").toString();
                String lastName = payload.get("family_name").toString();
                String email = payload.getEmail();
                return new TokenData(firstName, lastName, email);
            }
            throw new InvalidTokenException("Invalid Google OAuth Token");
        } catch (Exception e) {
            throw new InvalidTokenException("Invalid Token / Token could not be verified", e);
        }
    }

    /**
     * Method to create a JWT token with email, guid, and third-party token.
     *
     * @param email: the email of the user.
     * @param guid:  the unique identifier for the user.
     * @return the generated JWT token.
     */
    public String createJwt(@NotNull final String email, @NotNull final String guid) {
        Algorithm algorithm = Algorithm.HMAC256(key);
        return JWT.create().withClaim("guid", guid).sign(algorithm);
    }

    /**
     * Method to decode a JWT token and extract user information.
     *
     * @param jwt: the JWT token to be decoded.
     * @return JwtData containing user information extracted from the token.
     */
    public JwtData decodeJwt(@NotNull final String jwt) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(key);
            DecodedJWT decodedJWT = JWT.require(algorithm).build().verify(jwt);
            String guid = decodedJWT.getClaim("guid").asString();
            String thirdPartyToken = "";
            if (!decodedJWT.getClaim("thirdPartyToken").isNull()) {
                thirdPartyToken = decodedJWT.getClaim("thirdPartyToken").asString();
            }
            return new JwtData(guid, null, thirdPartyToken);
        } catch (JWTVerificationException | IllegalArgumentException e) {
            throw new InvalidTokenException(e);
        }
    }

    /**
     * Method to validate the JWT token in header.
     *
     * @param httpHeaders: The Http Headers in the request.
     * @return the {@link JwtValidationResult}.
     */
    public JwtValidationResult validateRequest(Map<String, String> httpHeaders) {
        String jwtToken = getJwtToken(httpHeaders);
        if (jwtToken == null || jwtToken.isBlank()) {
            log.error("JWT token is missing in the request headers.");
            return new JwtValidationResult(false, ResponseGenerator.generateFailureResponse(HttpStatus.UNAUTHORIZED, "JWT header token is missing"), null);
        }
        try {
            JwtData jwtData = decodeJwt(jwtToken);
            return new JwtValidationResult(true, null, jwtData);
        } catch (InvalidTokenException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            return new JwtValidationResult(false, ResponseGenerator.generateFailureResponse(HttpStatus.UNAUTHORIZED, "Invalid JWT token"), null);
        }
    }

    private String getJwtToken(Map<String, String> httpHeaders) {
        if (httpHeaders == null || httpHeaders.isEmpty()) {
            return null;
        }
        String jwtToken = httpHeaders.get(Constants.JWT_HEADER_KEY);
        if (jwtToken != null) {
            return jwtToken;
        }
        for (Map.Entry<String, String> header : httpHeaders.entrySet()) {
            if (Constants.JWT_HEADER_KEY.equalsIgnoreCase(header.getKey())) {
                return header.getValue();
            }
        }
        return null;
    }
}
