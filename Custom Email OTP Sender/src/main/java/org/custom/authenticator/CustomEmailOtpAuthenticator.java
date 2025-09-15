package org.custom.authenticator;

import jakarta.mail.MessagingException;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import org.custom.constant.OtpConfigKey;
import org.custom.constant.OtpHelper;
import org.custom.service.EmailSenderService;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.FormMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class CustomEmailOtpAuthenticator implements Authenticator {

    private static final Logger logger = LoggerFactory.getLogger(CustomEmailOtpAuthenticator.class);
    private EmailSenderService emailSenderService;

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
    public void authenticate(AuthenticationFlowContext context) {

        if (emailSenderService == null) {
            emailSenderService = new EmailSenderService(context.getRealm().getSmtpConfig());
        }
        var config = context.getAuthenticatorConfig().getConfig();

        var authSession = context.getAuthenticationSession();
        String otp = OtpHelper.generateNumericOtp(Integer.parseInt(config.get(OtpConfigKey.otpLength)));
        OtpHelper.storeOtp(authSession, otp, Integer.parseInt(config.get(OtpConfigKey.otpExpired)) * 60_000L);

        UserModel user = context.getUser();
        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();

        formData.forEach((k,v) -> System.out.println("Key is :: "  + k  + "  and value is :: "+ v));
        String inputType = formData.getFirst("inputType");
        String email = formData.getFirst("inputValue");

        if (!Objects.equals(inputType, "email") && email == null) {
            context.success();
            return;
        }

        try {
            validateEmail(email, context);
            emailSenderService.send(email, otp);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
        logger.info("OTP generated and email queued for {}", user.getUsername());

        context.challenge(createOTPInputForm(context));
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