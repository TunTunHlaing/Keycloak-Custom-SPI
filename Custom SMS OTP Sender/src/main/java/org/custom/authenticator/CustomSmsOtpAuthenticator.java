package org.custom.authenticator;

import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import org.custom.service.OTPService;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.FormMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomSmsOtpAuthenticator implements Authenticator {

    private static final Logger logger = LoggerFactory.getLogger(CustomSmsOtpAuthenticator.class);
    private OTPService otpService;

    @Override
    public void authenticate(AuthenticationFlowContext context) {

        if (otpService == null) {
            otpService = new OTPService(context.getAuthenticatorConfig().getConfig());
        }

        var user = context.getUser();

        if (user == null) {
            logger.error("User Not Found!");
            context.failure(AuthenticationFlowError.UNKNOWN_USER);
        }

        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
        String phoneNumber = formData.getFirst("phone");
        logger.info("Input PhoneNumber: " + phoneNumber);

        if (phoneNumber != null) {
            validatePhone(phoneNumber, context);

            String otp = otpService.generateOTP();
            otpService.storeOTP(otp, context.getSession());

            otpService.sendSMSOTP(phoneNumber, otp);

            logger.info("Creating OTP input form");
            context.challenge(createOTPInputForm(context));
        }else {
            context.success();
        }


    }


    private void validatePhone(String phoneNumber, AuthenticationFlowContext context) {
        if (!phoneNumber.matches("^\\+?[0-9]{7,15}$")) {
            context.failure(AuthenticationFlowError.INVALID_USER);
        }
    }

    private Response createOTPInputForm(AuthenticationFlowContext context) {
        return context.form()
                .setAttribute("realm", context.getRealm())
                .setAttribute("user", context.getUser())
                .createForm("otp-input.ftl");
    }

    @Override
    public void action(AuthenticationFlowContext context) {

        if (otpService == null) {
            otpService = new OTPService(context.getAuthenticatorConfig().getConfig());
        }

        String inputOtp = context.getHttpRequest().getDecodedFormParameters().getFirst("otp");
        if (inputOtp == null || inputOtp.isEmpty()) {
            logger.warn("No OTP provided");
            context.failure(AuthenticationFlowError.INVALID_CREDENTIALS);
            return;
        }

        if (otpService.validateOTP(inputOtp, context.getSession())) {
            logger.info("OTP validation successful for user: " + context.getUser().getUsername());
            context.success();
        } else {
            logger.warn("Invalid OTP provided");
            context.failureChallenge(AuthenticationFlowError.INVALID_CREDENTIALS, createOTPFailedForm(context,"Invalid OTP provided"));

        }
    }

    private Response createOTPFailedForm(AuthenticationFlowContext context, String invalidOtpProvided) {
        return context.form()
                .setAttribute("realm", context.getRealm())
                .setAttribute("user", context.getUser())
                .addError(new FormMessage(invalidOtpProvided))
                .createForm("otp-input.ftl");
    }

    @Override
    public boolean requiresUser() {
        return false;
    }

    @Override
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        return true;
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
    }

    @Override
    public void close() {
    }
}