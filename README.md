# Keycloak Custom SPI Authentication Modules

This repository contains several custom authentication modules for **Keycloak**. These modules extend Keycloak's authentication capabilities, allowing you to implement custom username forms, user creation flows, and OTP-based authentication via SMS and email.

## Modules

1. **Custom Username Form (User Creation If Not Exist) with reCAPTCHA**
2. **Custom OTP Sender (SMS)**
3. **Custom OTP Sender (Email)**

---

## Module 1: Custom Username Form

This module customizes the default username form in Keycloak. It allows you to modify the username input form used for authentication and user creation if not exist using reCAPTCHA.

### Features:
- Customizable username input field.
- Integration with Google reCAPTCHA for added security.
  
### Configuration:
1. Create an Authentication Flow in the Keycloak Admin Console.
2. Add the **Custom Username Form** authenticator to the flow.
3. Set up the Google reCAPTCHA keys (site key and secret) in the **Authenticator Configuration** tab.

### Usage:
- The user provides their username.
- The form will validate the input and display the reCAPTCHA widget if configured.
- After entering the correct username and passing reCAPTCHA, authentication proceeds.

---

## Module 2: Custom OTP Sender (SMS)

This module enables OTP (One-Time Password) based authentication via SMS. It integrates with **Twilio** to send OTPs to the user's phone number.

### Features:
- Sends OTP via SMS to the user's phone number.
- Validates the OTP entered by the user.

### Configuration:
1. Configure **Twilio Account SID**, **Auth Token**, and **Phone Number** and also OTP configuration like **OTP Length, OTP Expired and Max Attempt** in the **Authenticator Configuration** tab.
2. Add the **Custom OTP Sender SMS** authenticator to the authentication flow.

### Usage:
- The user enters their phone number.
- The system sends an OTP to the provided phone number via SMS.
- The user enters the OTP they received, and it is validated.

---

## Module 3: Custom OTP Sender (Email)

This module enables OTP-based authentication via email. It uses **SMTP** to send OTPs to the user's email address.

### Features:
- Sends OTP via email to the user's provided email address.
- Validates the OTP entered by the user.
  
### Configuration:
1. Configure **SMTP Host**, **Port**, **From Email**, **Username**, and **Password** in the **Realm Setting Email Configuration** tab.
2. Add the **Custom OTP Sender Email** authenticator to the authentication flow.

### Usage:
- The user enters their email address.
- The system generates and sends an OTP to the provided email address.
- The user enters the OTP they received, and it is validated.

---
