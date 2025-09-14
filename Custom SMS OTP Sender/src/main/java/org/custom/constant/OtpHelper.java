package org.custom.constant;

import org.keycloak.sessions.AuthenticationSessionModel;

import java.security.SecureRandom;

public final class OtpHelper {
    public static final String NOTE_OTP = "email-otp-code";
    public static final String NOTE_OTP_EXPIRE = "email-otp-expiry";
    public static final String NOTE_OTP_ATTEMPTS = "email-otp-attempts";
    private static final SecureRandom RNG = new SecureRandom();

    private OtpHelper() {}

    public static String generateNumericOtp(int digits) {
        int max = (int) Math.pow(10, digits);
        int n = RNG.nextInt(max); // secure
        return String.format("%0" + digits + "d", n);
    }

    public static void storeOtp(AuthenticationSessionModel authSession, String otp, long ttlMillis) {
        long expiry = System.currentTimeMillis() + ttlMillis;
        authSession.setAuthNote(NOTE_OTP, otp);
        authSession.setAuthNote(NOTE_OTP_EXPIRE, Long.toString(expiry));
        authSession.setAuthNote(NOTE_OTP_ATTEMPTS, "0");
    }

    public static boolean isOtpExpired(AuthenticationSessionModel authSession) {
        String exp = authSession.getAuthNote(NOTE_OTP_EXPIRE);
        if (exp == null) return true;
        try {
            return System.currentTimeMillis() > Long.parseLong(exp);
        } catch (NumberFormatException e) {
            return true;
        }
    }

    public static int incrementAttempts(AuthenticationSessionModel authSession) {
        String v = authSession.getAuthNote(NOTE_OTP_ATTEMPTS);
        int attempts = 0;
        try { attempts = v == null ? 0 : Integer.parseInt(v); } catch (NumberFormatException ignored) {}
        attempts++;
        authSession.setAuthNote(NOTE_OTP_ATTEMPTS, Integer.toString(attempts));
        return attempts;
    }

    public static boolean verifyOtp(AuthenticationSessionModel authSession, String input) {
        String stored = authSession.getAuthNote(NOTE_OTP);
        return stored != null && stored.equals(input) && !isOtpExpired(authSession);
    }
}
