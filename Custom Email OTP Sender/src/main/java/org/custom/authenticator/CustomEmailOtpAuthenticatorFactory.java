package org.custom.authenticator;

import org.custom.constant.OtpConfigKey;
import org.keycloak.Config;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;

import java.util.List;

public class CustomEmailOtpAuthenticatorFactory implements AuthenticatorFactory {

    private static final String DISPLAY_NAME = "Email OTP Sender";
    private static final String PROVIDER_ID = "email-otp-sender";

    @Override
    public String getDisplayType() {
        return DISPLAY_NAME;
    }

    @Override
    public String getReferenceCategory() {
        return "";
    }

    @Override
    public boolean isConfigurable() {
        return true;
    }

    @Override
    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        return new AuthenticationExecutionModel.Requirement[] {
                AuthenticationExecutionModel.Requirement.REQUIRED,
                AuthenticationExecutionModel.Requirement.ALTERNATIVE,
                AuthenticationExecutionModel.Requirement.DISABLED
        };
    }

    @Override
    public boolean isUserSetupAllowed() {
        return false;
    }

    @Override
    public String getHelpText() {
        return "Send OTP Via Email.";
    }


    private static ProviderConfigProperty constructProperty(String name, String label, String type,
                                                            String helpText, String defaultValue) {
        ProviderConfigProperty otpProperty = new ProviderConfigProperty();
        otpProperty.setName(name);
        otpProperty.setLabel(label);
        otpProperty.setType(type);
        otpProperty.setHelpText(helpText);
        otpProperty.setDefaultValue(defaultValue);
        return otpProperty;
    }


    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return List.of(
                constructProperty( OtpConfigKey.otpLength, "Otp Length", ProviderConfigProperty.STRING_TYPE, "Length for Otp.", "5"),
                constructProperty( OtpConfigKey.otpExpired, "Otp Expired",ProviderConfigProperty.STRING_TYPE, "Expired time for Otp in minutes.", "1"),
                constructProperty( OtpConfigKey.maxAttempt,"Max Attempt", ProviderConfigProperty.STRING_TYPE, "Max attempt for wrong Otp.", "5")
                );
    }


    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public Authenticator create(KeycloakSession keycloakSession) {
        return new CustomEmailOtpAuthenticator();
    }

    @Override
    public void init(Config.Scope scope) {

    }

    @Override
    public void postInit(KeycloakSessionFactory keycloakSessionFactory) {

    }

    @Override
    public void close() {

    }

}
