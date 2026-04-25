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
    public static final String INVOICE_PATH = VITALITY_API_BASE_PATH + "/invoice";
    public static final String INVOICE_UPLOAD_PATH = INVOICE_PATH + "/upload";
    public static final String INVOICE_STATUS_PATH = INVOICE_PATH + "/status/{jobId}";
    public static final String INVENTORY_PATH = VITALITY_API_BASE_PATH + "/inventory";
    public static final String ORDER_PATH = VITALITY_API_BASE_PATH + "/order";
    public static final String ORDER_INVOICE_PATH = "/invoice/{orderId}";

    public static final String JWT_HEADER_KEY = "token";
    public static final String ORDER_INVOICE_FILE_NAME = "order_invoice.pdf";
}
