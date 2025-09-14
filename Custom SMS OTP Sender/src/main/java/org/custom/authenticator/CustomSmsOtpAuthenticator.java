package org.custom.authenticator;

import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import org.custom.constant.OtpConfigKey;
import org.custom.constant.OtpHelper;
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

            var config = context.getAuthenticatorConfig().getConfig();
            var authSession = context.getAuthenticationSession();
            String otp = OtpHelper.generateNumericOtp(Integer.parseInt(config.get(OtpConfigKey.otpLength)));
            OtpHelper.storeOtp(authSession, otp, Integer.parseInt(config.get(OtpConfigKey.otpExpired)) * 60_000L);

            validatePhone(phoneNumber, context);
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

        var authSession = context.getAuthenticationSession();
        String inputOtp = context.getHttpRequest().getDecodedFormParameters().getFirst("otp");

        if (OtpHelper.isOtpExpired(authSession)) {
            context.failureChallenge(AuthenticationFlowError.EXPIRED_CODE, context.form()
                    .addError(new FormMessage("OTP expired. Please request a new one"))
                    .createForm("otp-input.ftl"));
            return;
        }

        int attempts = OtpHelper.incrementAttempts(authSession);
        if (attempts > Integer.parseInt(context.getAuthenticatorConfig().getConfig().get(OtpConfigKey.maxAttempt))) {
            logger.warn("Max OTP attempts exceeded for session {}", authSession.getAuthenticatedUser());
            context.failure(AuthenticationFlowError.INVALID_CREDENTIALS);
            return;
        }

        if (OtpHelper.verifyOtp(authSession, inputOtp)) {
            logger.info("OTP validated for user {}", context.getUser().getUsername());
            context.success();
        } else {
            context.failureChallenge(AuthenticationFlowError.INVALID_CREDENTIALS,
                    context.form().addError(new FormMessage("Invalid OTP")).createForm("otp-input.ftl"));
        }
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