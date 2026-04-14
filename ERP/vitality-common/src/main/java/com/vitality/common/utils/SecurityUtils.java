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
import com.vitality.common.dtos.TokenData;
import com.vitality.common.exceptions.InvalidTokenException;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;

import java.util.Collections;
import java.util.Objects;

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
}
