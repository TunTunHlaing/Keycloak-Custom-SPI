# Custom SMS OTP Authenticator for Keycloak

This custom authenticator allows OTP (One-Time Password) authentication via SMS for Keycloak users. It uses **Twilio** as the SMS service to send OTPs and validates the OTP entered by the user.

## Configuration

### 1. Set up Twilio Credentials

To configure this authenticator, you must provide **Twilio** credentials:

![OTP SMS configure flow](src/main/resources/images/custom-sms.png)

- **Twilio Account SID**: The SID from your Twilio account.
- **Twilio Auth Token**: The authentication token for your Twilio account.
- **Twilio Phone Number**: The phone number from which the OTP will be sent.

These credentials should be added to the **Authenticator Configuration**:

1. Navigate to the **Keycloak Admin Console**.
2. Under **Authentication**, create or edit an existing flow.
3. Add the **Custom SMS OTP Authenticator** to the flow.
4. In the **Authenticator Configurations** tab, provide the **Twilio SID**, **Auth Token**, and **Phone Number** values.

### 2. Phone Number Validation

The **CustomSmsOtpAuthenticator** validates the phone number provided by the user before sending the OTP. It uses a regular expression to ensure that the phone number matches a valid format (`+?[0-9]{7,15}`).

### 3. OTP Generation and SMS Sending

- The authenticator generates a random OTP.
- The OTP is sent to the user's phone via **Twilio SMS**.
- The OTP is stored temporarily for validation.

---

## Usage

### Authentication Flow

1. The user enters their **phone number** in the login form. (login form must pass **phone** fields to work this flow)
2. The **Custom SMS OTP Authenticator** validates the phone number and generates an OTP.
3. The OTP is sent to the user's phone via **Twilio**.
4. The user is presented with an OTP input form to enter the OTP they received.
5. The user submits the OTP for validation:
    - If the OTP is valid, authentication is successful.
    - If the OTP is invalid, the user is prompted again.

### OTP Input Form

The user is prompted to enter the OTP they received via SMS in the form presented by the authenticator.

---

## Components

### 1. **CustomSmsOtpAuthenticator**

This is the main class implementing the `Authenticator` interface. It performs the following tasks:
- Validates the phone number.
- Generates and stores the OTP.
- Sends the OTP via SMS using the **Twilio API**.
- Displays an OTP input form for the user to enter their OTP.

### 2. **OTPService**

The `OTPService` class handles the OTP generation, validation, and storage:
- Generates a random 6-digit OTP.
- Sends the OTP via SMS using **Twilio**.
- Validates the OTP provided by the user.
- Stores OTPs temporarily in the **Keycloak session**.

### 3. **CustomSmsOtpAuthenticatorFactory**

This factory class configures the **CustomSmsOtpAuthenticator** and its settings in the **Keycloak Admin Console**:
- Displays the authenticator name (`Custom OTP Sender`).
- Provides configuration properties for the **Twilio** credentials (SID, Auth Token, and Phone Number).

---

## Twilio Setup

To use this authenticator, you'll need a **Twilio** account:

1. Go to [Twilio](https://www.twilio.com/) and sign up for an account.
2. Once signed in, navigate to the **Dashboard** to get your **Account SID** and **Auth Token**.
3. Buy a **Twilio phone number** from the **Twilio Console** to send SMS.

---

## Example: OTP Input Form

Here is the HTML form displayed to the user where they will enter their OTP:

```html
<@layout.registrationLayout displayMessage=true; section>
  <form action="${url.loginAction}" method="post">
    <div class="form-group">
      <label for="otp">Enter OTP</label>
      <input type="text" id="otp" name="otp" class="form-control" autofocus />
    </div>
    <div class="form-group">
      <button type="submit" class="btn btn-primary">Verify</button>
    </div>
  </form>
</@layout.registrationLayout>
```

### Notes:
1. **Customization**: You can customize  the logic based on your requirements.
2. **Keycloak Integration**: Make sure you understand how to deploy and configure Keycloak with custom SPIs. You may need to restart your Keycloak instance after deploying the JAR.
