package org.custom.authenticator;

import jakarta.mail.MessagingException;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import org.custom.service.EmailSenderService;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomEmailOtpAuthenticator implements Authenticator {

    private static final Logger logger = LoggerFactory.getLogger(CustomEmailOtpAuthenticator.class);
    private EmailSenderService emailSenderService;

    @Override
    public void authenticate(AuthenticationFlowContext context) {

        var user = context.getUser();

        if (user == null) {
            logger.warn("No username provided");
            context.failure(AuthenticationFlowError.UNKNOWN_USER);
            return;
        }

        if (emailSenderService == null) {
            emailSenderService = new EmailSenderService(context.getRealm().getSmtpConfig());
        }

        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
        String email = formData.getFirst("email");
        logger.info("Input email: " + email);

        if (email != null){
            validateEmail(email, context);
            var otp = emailSenderService.generateOTP();
            emailSenderService.storeOTP(otp, context.getSession());
        try {
            emailSenderService.send(email, otp);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }

        logger.info("Creating OTP input form");
        context.challenge(createOTPInputForm(context));
        }else {
            context.success();
        }
    }

    private Response createOTPInputForm(AuthenticationFlowContext context) {
        return context.form()
                .setAttribute("realm", context.getRealm())
                .setAttribute("user", context.getUser())
                .createForm("otp-input.ftl");
    }

    private void validateEmail(String email, AuthenticationFlowContext context) {
        if (!email.matches("^[\\\\w.-]+@[\\\\w.-]+\\\\.[a-zA-Z]{2,}$")) {
            context.failure(AuthenticationFlowError.INVALID_USER);
        }
    }

    @Override
    public void action(AuthenticationFlowContext context) {
        String inputOtp = context.getHttpRequest().getDecodedFormParameters().getFirst("otp");
        if (inputOtp == null || inputOtp.isEmpty()) {
            logger.warn("No OTP provided");
            context.failure(AuthenticationFlowError.INVALID_CREDENTIALS);
            return;
        }

        if (emailSenderService.validateOTP(inputOtp, context.getSession())) {
            logger.info("OTP validation successful for user: " + context.getUser().getUsername());
            context.success();
        } else {
            logger.warn("Invalid OTP provided");
            context.failure(AuthenticationFlowError.INVALID_CREDENTIALS);
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