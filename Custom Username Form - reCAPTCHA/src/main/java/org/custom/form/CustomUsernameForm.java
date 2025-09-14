package org.custom.form;

import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;
import org.keycloak.authentication.AbstractFormAuthenticator;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.AuthenticatorConfigModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.FormMessage;

import java.util.Collections;

public class CustomUsernameForm extends AbstractFormAuthenticator {

    public static final String G_RECAPTCHA_RESPONSE = "g-recaptcha-response";
    public static final String SITE_KEY = "site.key";
    public static final String SITE_SECRET = "secret";
    public static final String USE_RECAPTCHA_NET = "useRecaptchaNet";
    private static final Logger logger = Logger.getLogger(CustomUsernameForm.class);

    private String siteKey;

    @Override
    public void authenticate(AuthenticationFlowContext context) {
        Response form = createUsernameForm(context).createForm("custom-username-form.ftl");
        context.challenge(form);
    }
    @Override
    public void action(AuthenticationFlowContext context) {

        logger.info("Custom User name Form Starting...");
        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
        String username = formData.getFirst("username");

        if (username == null || username.trim().isEmpty()) {
            LoginFormsProvider form = createUsernameForm(context);
            form.setErrors(Collections.singletonList(new FormMessage("Username is required.")));
            context.failureChallenge(AuthenticationFlowError.INVALID_USER, form.createForm("custom-username-form.ftl"));
            return;
        }

        boolean captchaSuccess = RecaptchaUtils.recaptchaAction(context, SITE_SECRET, G_RECAPTCHA_RESPONSE, USE_RECAPTCHA_NET);

        if ("dev".equals(System.getProperty("quarkus.profile"))) {
            captchaSuccess = true;
        }

        if (!captchaSuccess) {
            LoginFormsProvider form = createUsernameForm(context);
            form.setAttribute("username", username);
            form.setErrors(Collections.singletonList(new FormMessage("Invalid reCAPTCHA. Please try again.")));
            context.failureChallenge(AuthenticationFlowError.INVALID_CREDENTIALS, form.createForm("custom-username-form.ftl"));
            return;
        }


        UserModel user = context.getSession().users().getUserByUsername(context.getRealm(), username);
        if (user == null) {
            user = context.getSession().users().addUser(context.getRealm(), username);
            user.setEnabled(true);
        }
        context.setUser(user);
        context.success();
    }

    private LoginFormsProvider createUsernameForm(AuthenticationFlowContext context) {
        LoginFormsProvider form = context.form()
                .setAttribute("username", context.getHttpRequest().getDecodedFormParameters().getFirst("username"))
                .setAttribute("recaptchaRequired", true);

        AuthenticatorConfigModel captchaConfig = context.getAuthenticatorConfig();
        if (captchaConfig != null && captchaConfig.getConfig() != null) {
            siteKey = captchaConfig.getConfig().get(SITE_KEY);
            form.setAttribute("recaptchaSiteKey", siteKey);
            form.addScript("https://www." + RecaptchaUtils.getRecaptchaDomain(captchaConfig, USE_RECAPTCHA_NET) + "/recaptcha/api.js");
        }

        return form;
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
}
