package com.vitality.common.utils;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;

@Slf4j
public class MessagingUtils {
    @Value("${twilio.account.sid}")
    private String twilioAccountSid;
    @Value("${twilio.auth.token}")
    private String twilioAuthToken;
    @Value("${twilio.phone.number}")
    private String fromPhoneNumber;

    /**
     * Method to send the OTP message to the given phone number.
     *
     * @param phoneNumber: the recipient phone number.
     * @param otp:         the OTP to be sent.
     */
    public void sendOTPMessage(@NotNull final String phoneNumber, @NotNull final String otp) {
        Twilio.init(twilioAccountSid, twilioAuthToken);
        Message.creator(new PhoneNumber(phoneNumber),
                new PhoneNumber(fromPhoneNumber),
                Constants.OTP_MESSAGE + otp).create();
        log.info("Sent OTP {} to phone number {}", otp, phoneNumber);
    }
}
