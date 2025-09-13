package org.custom.service;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.custom.SmtpConfigKey;
import org.keycloak.models.KeycloakSession;

import java.util.Map;
import java.util.Properties;

public class EmailSenderService {

    private String fromEmail;
    private String smtpPort;
    private Session session;

    public EmailSenderService(Map<String, String> smtpConfig) {

        smtpPort = smtpConfig.get(SmtpConfigKey.PORT.getKeyName());
        fromEmail = smtpConfig.get(SmtpConfigKey.FROM.getKeyName());
        var smtpHost = smtpConfig.get(SmtpConfigKey.HOST.getKeyName());
        var smtpUsername = smtpConfig.get(SmtpConfigKey.FROM.getKeyName());
        var smtpPassword = smtpConfig.get(SmtpConfigKey.PASSWORD.getKeyName());

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", smtpHost);
        props.put("mail.smtp.port", smtpPort);


        session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(smtpUsername, smtpPassword);
            }
        });

    }
    public void storeOTP(String otp, KeycloakSession session) {
        session.getContext().getRealm().setAttribute("otp_" + otp, otp);
        session.getContext().getRealm().setAttribute("otp_expiry_" + otp, System.currentTimeMillis() + 300000);
    }

    public boolean validateOTP(String otp, KeycloakSession session) {
        String storedOtp = session.getContext().getRealm().getAttribute("otp_" + otp);
        Long expiryTime = Long.valueOf(session.getContext().getRealm().getAttribute("otp_expiry_" + otp));
        return storedOtp != null && System.currentTimeMillis() < expiryTime;
    }

    public String generateOTP() {
        return String.valueOf((int) (Math.random() * 1000000)); }


    public void send(String toEmail, String otp) throws MessagingException {
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(fromEmail));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
        message.setSubject("Your OTP Code");
        message.setText("Your OTP is: " + otp);

        Transport.send(message);
    }
}

