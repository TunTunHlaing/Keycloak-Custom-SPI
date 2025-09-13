package org.custom.authenticator;

import org.custom.TwilioKey;
import org.keycloak.Config;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;

import java.util.List;

public class CustomSmsOtpAuthenticatorFactory implements AuthenticatorFactory {

    private static final String DISPLAY_NAME = "Custom OTP Sender";
    private static final String PROVIDER_ID = "custom_sms_sender";

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
        return "Send Sms OTP Using Twilio.";
    }
    @Override
    public List<ProviderConfigProperty> getConfigProperties() {

        return List.of(
                constructOtpProperty(TwilioKey.SID.getKeyname(), "Twilio Account SID", ProviderConfigProperty.STRING_TYPE, "Twilio SID for sending SMS"),
                constructOtpProperty(TwilioKey.AUTH.getKeyname(), "Twilio Auth Token", ProviderConfigProperty.STRING_TYPE, "Twilio Auth Token"),
                constructOtpProperty(TwilioKey.PHONE.getKeyname(), "Twilio Phone Number", ProviderConfigProperty.STRING_TYPE, "Sender phone number for Twilio")
        );
    }


    private ProviderConfigProperty constructOtpProperty(String name, String label, String type, String helpText) {
        ProviderConfigProperty otpProperty = new ProviderConfigProperty();
        otpProperty.setName(name);
        otpProperty.setLabel(label);
        otpProperty.setType(type);
        otpProperty.setHelpText(helpText);
        return otpProperty;
    }

    @Override
    public Authenticator create(KeycloakSession keycloakSession) {
        return new CustomSmsOtpAuthenticator();
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

    @Override
    public String getId() {
        return PROVIDER_ID;
    }
}
