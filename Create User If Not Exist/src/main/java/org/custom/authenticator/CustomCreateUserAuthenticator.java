package org.custom.authenticator;

import jakarta.ws.rs.core.MultivaluedMap;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomCreateUserAuthenticator implements Authenticator {

    private static final Logger logger = LoggerFactory.getLogger(CustomCreateUserAuthenticator.class);

    @Override
    public void authenticate(AuthenticationFlowContext context) {
        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
        String inputUsername = formData.getFirst("username");
        logger.info("Input Username: " + inputUsername);

        if (inputUsername == null || inputUsername.isEmpty()) {
            logger.warn("No username provided");
            context.failure(AuthenticationFlowError.INVALID_USER);
            return;
        }

        UserModel user = context.getSession().users().getUserByUsername(context.getRealm(), inputUsername);
        if (user == null) {
            logger.info("User not found, creating new user: " + inputUsername);
            user = context.getSession()
                    .users()
                    .addUser(context.getRealm(), inputUsername);
            user.setEnabled(true);
            context.setUser(user);
        } else {
            if (!user.isEnabled()) {
                logger.warn("User is disabled: " + inputUsername);
                user.setEnabled(true);
                logger.info("Enabled user: " + inputUsername);

            }
            context.setUser(user);
        }
    }

    @Override
    public void action(AuthenticationFlowContext context) {

    }

    @Override
    public boolean requiresUser() {
        return false;
    }

    @Override
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        return false;
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {

    }

    @Override
    public void close() {

    }
}
