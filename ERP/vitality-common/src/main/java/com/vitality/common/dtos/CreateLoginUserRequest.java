package com.vitality.common.dtos;

import lombok.Data;

import java.util.Date;

@Data
public class CreateLoginUserRequest {
    private String email;
    private String firstName;
    private String lastName;
    private String googleToken;
    private String phoneNumber;
    private Date dateOfBirth;
    private String gender;
}
