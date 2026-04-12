package com.vitality.common.utils;

import com.vitality.common.dtos.CreatePrescriptionDiagnosisRequest;
import com.vitality.common.dtos.CreatePrescriptionRequest;
import com.vitality.common.exceptions.InvalidRequestException;

import java.util.List;

public class Validators {
    public static void validatePrescriptionDiagnosis(CreatePrescriptionRequest request) {
        List<CreatePrescriptionDiagnosisRequest> diagnosisRequest = request.getPrescriptionDiagnoses();
        if (diagnosisRequest == null || diagnosisRequest.isEmpty()) {
            throw new InvalidRequestException("At least one prescription diagnosis is required");
        }
        for (CreatePrescriptionDiagnosisRequest diagnosis : diagnosisRequest) {
            if (diagnosis.getMedicineName() == null || diagnosis.getMedicineName().isBlank()) {
                throw new InvalidRequestException("Medicine name cannot be blank");
            }
            if (diagnosis.getStartDate() == null) {
                throw new InvalidRequestException("Start date is required");
            }
        }
    }
}
