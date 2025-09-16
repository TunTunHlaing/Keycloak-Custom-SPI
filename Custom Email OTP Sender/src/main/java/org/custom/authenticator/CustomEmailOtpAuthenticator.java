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
import org.keycloak.forms.login.MessageType;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.FormMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

public class CustomEmailOtpAuthenticator implements Authenticator {

    private static final Logger logger = LoggerFactory.getLogger(CustomEmailOtpAuthenticator.class);
    private static final String DATE_TIME_FORMAT = "dd/MM/yyyy HH:mm:ss";
    private EmailSenderService emailSenderService;

    private Response createOTPInputForm(AuthenticationFlowContext context, String[] args) {
        var form = context.form()
                .setAttribute("realm", context.getRealm())
                .setAttribute("user", context.getUser());

        for (String arg : args) {
            form.setMessage(MessageType.INFO, arg);
        }

        return form.createForm("otp-input.ftl");
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
            var otpExpired = LocalDateTime.now().plusMinutes(Integer.parseInt(config.get(OtpConfigKey.otpExpired)));
            user.setAttribute("otp", List.of(otp));
            user.setAttribute("otp_expired", List.of(otpExpired.format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT))));
            emailSenderService.send(email, otp);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
        logger.info("OTP generated and email queued for {}", user.getUsername());

        context.challenge(createOTPInputForm(context, new String[]{}));
    }

    @Override
    public void action(AuthenticationFlowContext context) {

        if (emailSenderService == null) {
            emailSenderService = new EmailSenderService(context.getRealm().getSmtpConfig());
        }

        var authSession = context.getAuthenticationSession();
        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
        var config = context.getAuthenticatorConfig().getConfig();

        String resendOtp = formData.getFirst("resendOtp");

        if ("true".equals(resendOtp)) {
            String otp = OtpHelper.generateNumericOtp(Integer.parseInt(config.get(OtpConfigKey.otpLength)));
            OtpHelper.storeOtp(authSession, otp, Integer.parseInt(config.get(OtpConfigKey.otpExpired)) * 60_000L);
            var user = context.getUser();

            String email = user.getUsername();
            try {
                emailSenderService.send(email, otp);
                var otpExpired = LocalDateTime.now().plusMinutes(Integer.parseInt(config.get(OtpConfigKey.otpExpired)));

                user.setAttribute("otp", List.of(otp));
                user.setAttribute("otp_expired", List.of(otpExpired.format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT))));
                logger.info("OTP resent to {}", email);

                context.challenge(createOTPInputForm(context, new String[]{"OTP has been resent"}));
            } catch (MessagingException e) {
                throw new RuntimeException("Error sending OTP email", e);
            }

            return;
        }

        String inputOtp = formData.getFirst("otp");

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