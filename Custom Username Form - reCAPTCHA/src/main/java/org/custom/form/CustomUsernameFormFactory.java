package org.custom.form;

import org.keycloak.Config;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;

import java.util.ArrayList;
import java.util.List;

public class CustomUsernameFormFactory implements AuthenticatorFactory {

    public static final String PROVIDER_ID = "custom-username-form";
    private static final List<ProviderConfigProperty> CONFIG_PROPERTIES = new ArrayList<>();

    static {
        CONFIG_PROPERTIES.addAll(List.of(constructProperty(
                        CustomUsernameForm.SITE_KEY,
                        "Recaptcha Site Key",
                        ProviderConfigProperty.STRING_TYPE,
                        "Google Recaptcha Site Key"
                ),

                constructProperty(
                        CustomUsernameForm.SITE_SECRET,
                        "Recaptcha Secret",
                        ProviderConfigProperty.STRING_TYPE,
                        "Google Recaptcha Secret"
                ),

                constructProperty(
                        CustomUsernameForm.USE_RECAPTCHA_NET,
                        "Use recaptcha.net",
                        ProviderConfigProperty.BOOLEAN_TYPE,
                        "Use recaptcha.net instead of google.com"
                )));

    }

    private static ProviderConfigProperty constructProperty(String name, String label, String type, String helpText) {
        ProviderConfigProperty otpProperty = new ProviderConfigProperty();
        otpProperty.setName(name);
        otpProperty.setLabel(label);
        otpProperty.setType(type);
        otpProperty.setHelpText(helpText);
        return otpProperty;
    }


    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public String getDisplayType() {
        return "Custom Username Form";
    }

    @Override
    public String getHelpText() {
        return "Collects username without validating user existence";
    }

    @Override
    public String getReferenceCategory() {
        return "username";
    }

    @Override
    public boolean isConfigurable() {
        return true;
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return CONFIG_PROPERTIES;
    }

    @Override
    public Authenticator create(KeycloakSession session) {
        return new CustomUsernameForm();
    }

    @Override
    public void init(Config.Scope config) {
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
    }

    @Override
    public void close() {
    }

    @Override
    public boolean isUserSetupAllowed() {
        return false;
    }

    @Override
    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        return new AuthenticationExecutionModel.Requirement[] {
                AuthenticationExecutionModel.Requirement.REQUIRED,
                AuthenticationExecutionModel.Requirement.ALTERNATIVE
        };
    }
}
