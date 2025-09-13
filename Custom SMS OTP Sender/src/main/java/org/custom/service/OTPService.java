package org.custom.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.custom.TwilioKey;
import org.keycloak.models.KeycloakSession;
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

    public String generateOTP() {
        return String.valueOf((int) (Math.random() * 1000000)); }


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

    public void storeOTP(String otp, KeycloakSession session) {
        session.getContext().getRealm().setAttribute("otp_" + otp, otp);
        session.getContext().getRealm().setAttribute("otp_expiry_" + otp, System.currentTimeMillis() + 300000);
    }

    public boolean validateOTP(String otp, KeycloakSession session) {
        try {
            String storedOtp = session.getContext().getRealm().getAttribute("otp_" + otp);
            Long expiryTime = Long.valueOf(session.getContext().getRealm().getAttribute("otp_expiry_" + otp));
            return storedOtp != null && System.currentTimeMillis() < expiryTime;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}



