package com.vitality.common.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CreatePrescriptionDiagnosisRequest {
    private String diagnosis;
    @Valid
    @NotBlank(message = "Medicine name cannot be blank")
    private String medicineName;
    private String dosage;
    private BigDecimal unit;
    private String unitMeasure;
    @NotEmpty(message = "Start date is required")
    private LocalDate startDate;
    private LocalDate endDate;
    private String frequency;
}
