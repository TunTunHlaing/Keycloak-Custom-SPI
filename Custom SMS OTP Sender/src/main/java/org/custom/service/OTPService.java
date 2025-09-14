package org.custom.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.custom.TwilioKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class OTPService {

    private final Logger logger = LoggerFactory.getLogger(OTPService.class);

    private String twilioAccountSid;
    private String twilioAuthToken;
    private String twilioPhoneNumber;

    public OTPService(Map<String, String> config) {
        this.twilioAccountSid = config.get(TwilioKey.SID.getKeyname());
        this.twilioAuthToken = config.get(TwilioKey.AUTH.getKeyname());
        this.twilioPhoneNumber = config.get(TwilioKey.PHONE.getKeyname());
    }

    public void sendSMSOTP(String phoneNumber, String otp) {
        try {
            Twilio.init(twilioAccountSid, twilioAuthToken);
            Message.creator(
                            new PhoneNumber(phoneNumber),
                            new PhoneNumber(twilioPhoneNumber),
                            "Your OTP is: " + otp)
                    .create();

            logger.info("SMS OTP sent successfully to {}" , phoneNumber);
        } catch (Exception e) {
            logger.error("Failed to send SMS OTP to " + phoneNumber + ": " + e.getMessage());
            throw new RuntimeException("SMS sending failed", e);
        }
    }

}



