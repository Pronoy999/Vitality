package com.vitality.common.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.util.List;

@EqualsAndHashCode(callSuper = false)
@Data
public class CreatePrescriptionRequest extends CreatePatientRequest {
    private LocalDate prescriptionDate;
    private String referredByDoctor;
    @NotEmpty(message = "Prescription image URL cannot be blank")
    private String prescriptionImageUrl;
    private String diagnosis;
    @Valid
    @NotEmpty(message = "At least one prescription diagnosis is required")
    private List<CreatePrescriptionDiagnosisRequest> prescriptionDiagnoses;
}
