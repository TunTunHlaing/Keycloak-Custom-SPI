package org.custom;

public enum TwilioKey {
    SID("twilio_account_sid"),
    AUTH("twilio_auth_token"),
    PHONE("twilio_phone_number");

    private String keyname;

    public String getKeyname() {
        return keyname;
    }

    TwilioKey(String keyname) {
        this.keyname = keyname;
    }
}
