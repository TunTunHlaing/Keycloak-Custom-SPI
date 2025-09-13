package org.custom.form;

import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;
import org.keycloak.authentication.AbstractFormAuthenticator;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.events.Details;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.AuthenticatorConfigModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.FormMessage;
import org.keycloak.services.messages.Messages;


public class CustomUsernameForm extends AbstractFormAuthenticator {

    public static final String G_RECAPTCHA_RESPONSE = "g-recaptcha-response";
    public static final String SITE_KEY = "site.key";
    public static final String SITE_SECRET = "secret";
    public static final String USE_RECAPTCHA_NET = "useRecaptchaNet";
    private static final Logger logger = Logger.getLogger(CustomUsernameForm.class);

    private String siteKey;

    @Override
    public void authenticate(AuthenticationFlowContext context) {
        Response form = createUsernameForm(context);
        context.challenge(form);
    }


    @Override
    public void action(AuthenticationFlowContext context) {

        RecaptchaUtils.recaptchaAction(context,SITE_KEY,
                G_RECAPTCHA_RESPONSE, USE_RECAPTCHA_NET);
        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
        String username = formData.getFirst("username");
        if (username == null || username.trim().isEmpty()) {
            context.getEvent().error("invalid_username");
            Response form = createUsernameForm(context);
            context.failureChallenge(AuthenticationFlowError.INVALID_USER, form);
            return;
        }

        context.success();
    }

    private Response createUsernameForm(AuthenticationFlowContext context) {
        LoginFormsProvider form = context.form()
                .setAttribute("username",
                        context.getHttpRequest().getDecodedFormParameters().getFirst("username"));
        form.setAttribute("recaptchaRequired", true);
        form.setAttribute("recaptchaSiteKey", siteKey);
        context.getEvent().detail(Details.AUTH_METHOD, "auth_method");
        if (logger.isInfoEnabled()) {
            logger.info(
                    "validateRecaptcha(AuthenticationFlowContext, boolean, String, String) - Before the validation");
        }

        AuthenticatorConfigModel captchaConfig = context.getAuthenticatorConfig();

        if (captchaConfig == null || captchaConfig.getConfig() == null
                || captchaConfig.getConfig().get(SITE_KEY) == null
                || captchaConfig.getConfig().get(SITE_SECRET) == null) {
            form.addError(new FormMessage(null, Messages.RECAPTCHA_NOT_CONFIGURED));
        }
        siteKey = captchaConfig.getConfig().get(SITE_KEY);
        form.setAttribute("recaptchaRequired", true);
        form.setAttribute("recaptchaSiteKey", siteKey);
        form.addScript("https://www." + RecaptchaUtils.getRecaptchaDomain(captchaConfig, USE_RECAPTCHA_NET) + "/recaptcha/api.js" );
        return form.createForm("custom-username-form.ftl");
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
