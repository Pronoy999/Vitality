package com.vitality.common.dtos;

public record CreatePatientRequest(String name, String phoneNumber, String email, int age, int height, int weight,
                                   String gender, String bloodPressure, String ailmentHistory,
                                   boolean hasHealthInsurance, String additionalDiagnosis, String medicinesConsumed,
                                   String additionalServicesRequired,String abhaId) {
}
