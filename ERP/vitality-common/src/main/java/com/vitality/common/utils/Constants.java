package com.vitality.common.utils;

public class Constants {
    /*
        Path Constants for Vitality API endpoints
     */
    public static final String OTP_MESSAGE = "Your Vitality OTP code is: ";
    public static final String VITALITY_API_BASE_PATH = "/api/v1/vitality";
    public static final String PATIENT_PATH = VITALITY_API_BASE_PATH + "/patient";
    public static final String USER_PATH = VITALITY_API_BASE_PATH + "/user";
    public static final String PRESCRIPTION_PATH = VITALITY_API_BASE_PATH + "/prescription";
    public static final String PRESCRIPTION_UPLOAD_PATH = PRESCRIPTION_PATH + "/upload";
    public static final String PRESCRIPTION_STATUS_PATH = PRESCRIPTION_PATH + "/status/{jobId}";
    public static final String PRESCRIPTION_CONFIRM_PATH = PRESCRIPTION_PATH + "/confirm/{jobId}";
    public static final String PRESCRIPTION_MANUAL_PATH = PRESCRIPTION_PATH + "/manual";
    public static final String HEALTH_CHECK_PATH = VITALITY_API_BASE_PATH + "/health";

    public static final String JWT_HEADER_KEY = "token";
}
