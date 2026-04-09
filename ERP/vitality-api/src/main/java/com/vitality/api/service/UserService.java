package com.vitality.api.service;

import com.vitality.api.entities.Credentials;
import com.vitality.api.entities.User;
import com.vitality.api.repositories.CredentialsRepository;
import com.vitality.api.repositories.UserRepository;
import com.vitality.common.dtos.CreateLoginUserRequest;
import com.vitality.common.dtos.LoginUserResponse;
import com.vitality.common.dtos.TokenData;
import com.vitality.common.exceptions.InvalidTokenException;
import com.vitality.common.utils.GuidUtils;
import com.vitality.common.utils.ResponseGenerator;
import com.vitality.common.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final CredentialsRepository credentialsRepository;
    private final SecurityUtils securityUtils;

    public ResponseEntity<?> createOrLoginUser(CreateLoginUserRequest request) {
        if (request.getEmail() == null && request.getGoogleToken() == null) {
            return ResponseGenerator.generateFailureResponse(HttpStatus.BAD_REQUEST, "Email or Google Token must be provided");
        }
        TokenData tokenData;
        try {
            tokenData = securityUtils.verifyGoogleOAuthToken(request.getGoogleToken());
        } catch (InvalidTokenException e) {
            log.error("Invalid Google Token");
            return ResponseGenerator.generateFailureResponse(HttpStatus.UNAUTHORIZED, "Invalid Google Token");
        }
        try {
            User user = getUserByEmail(tokenData.emailId());
            if (user != null) {
                String jwtToken = securityUtils.createJwt(tokenData.emailId(), user.getGuid());
                LoginUserResponse response = new LoginUserResponse(jwtToken, user.getGuid());
                return ResponseGenerator.generateSuccessResponse(response, HttpStatus.OK);
            } else {
                String guid = GuidUtils.generateGuid();
                User newUser = createUser(tokenData, guid);
                createCredentials(tokenData.emailId(), request.getGoogleToken(), newUser);
                String jwtToken = securityUtils.createJwt(tokenData.emailId(), guid);
                LoginUserResponse response = new LoginUserResponse(jwtToken, guid);
                return ResponseGenerator.generateSuccessResponse(response, HttpStatus.OK);
            }
        } catch (Exception e) {
            log.error("Error during user creation/login: {}", e.getMessage());
            return ResponseGenerator.generateFailureResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while processing the request");
        }
    }

    private User createUser(TokenData tokenData, String guid) {
        User newUser = new User();
        newUser.setGuid(guid);
        newUser.setFirstName(tokenData.firstName());
        newUser.setLastName(tokenData.lastName());
        newUser.setActive(true);
        newUser = userRepository.save(newUser);
        return newUser;
    }

    private void createCredentials(String emailId, String googleToken, User user) {
        Credentials credentials = new Credentials();
        credentials.setUser(user);
        credentials.setEmailId(emailId);
        credentials.setGoogleToken(googleToken);
        credentialsRepository.save(credentials);
    }

    private User getUserByEmail(String email) {
        Credentials credentials = credentialsRepository.findCredentialsByEmailId(email);
        if (credentials != null) {
            return credentials.getUser();
        }
        return null;
    }
}
