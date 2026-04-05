package com.vitality.common.utils;

import java.security.SecureRandom;

public class OtpGenerator {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * Method to generate a 4-digit OTP.
     *
     * @return the generated OTP in {@link String} format.
     */
    public static String generate4DigitOtp() {
        int otp = SECURE_RANDOM.nextInt(9000) + 1000; // range 1000–9999
        return String.valueOf(otp);
    }
}