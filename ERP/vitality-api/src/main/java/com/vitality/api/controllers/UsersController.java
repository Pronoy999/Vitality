package com.vitality.api.controllers;

import com.vitality.api.service.UserService;
import com.vitality.common.dtos.CreateLoginUserRequest;
import com.vitality.common.utils.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@Slf4j
@RestController("userController")
@RequestMapping(Constants.USER_PATH)
public class UsersController {
    private final UserService userService;

    @PostMapping
    public ResponseEntity<?> createOrLoginUser(@RequestBody CreateLoginUserRequest request) {
        log.info("Received request to create/login user");
        return userService.createOrLoginUser(request);
    }
}
