package com.vitality.common.dtos;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@Data
public class CreatePatientRequest {
    @NotEmpty(message = "First name cannot be empty")
    private String firstName;
    @NotEmpty(message = "Last name cannot be empty")
    private String lastName;
    private String phoneNumber;
    private String email;
    private Integer age;
    private BigDecimal height;
    private BigDecimal weight;
    private String gender;
    private String bloodPressure;
    private String ailmentHistory;
    private Boolean hasHealthInsurance;
    private String additionalDiagnosis;
    private String medicinesConsumed;
    private String additionalServicesRequired;
    private String healthParameters;
    private String abhaId;
}