# Custom Email OTP Authenticator for Keycloak

This custom authenticator allows OTP (One-Time Password) authentication via email for Keycloak users. It uses the realm's **SMTP configuration** to send OTPs to the user's email address, and validates the OTP entered by the user.

---

## Configuration

### 1. Set up SMTP Configuration

To configure this authenticator, you need to provide SMTP credentials to send emails. These credentials should be added in the **Realm Settings → Email** section:

1. Navigate to the **Keycloak Admin Console**.
2. Under **Realm Settings**, go to the **Email** tab.
3. Configure SMTP host, port, username, and password.

![OTP configure flow](src/main/resources/images/email-configure.png)

---

### 2. Authentication Flow

1. Navigate to the **Keycloak Admin Console**.
2. Under **Authentication**, create or edit an existing flow.
3. Add the **Custom Email OTP Authenticator** to the flow (`Email OTP Sender`).
4. Set its requirement (REQUIRED / ALTERNATIVE).

---

## Usage

### Authentication Flow

1. The user enters their **email address** in the login form.  
   *(The login form must submit the value in the `username` (email value) field.)*
2. The **Custom Email OTP Authenticator** validates the email format.
3. An OTP is generated and stored in the authentication session.
4. The OTP is sent to the user’s email via SMTP.
5. The user is shown an OTP input form (`otp-input.ftl`) to enter the OTP.
6. The user submits the OTP:
   -  If valid → authentication succeeds.
   -  If invalid → user is prompted again until max attempts are reached.
   - ️ If expired → user must request a new OTP.

---

## Components

### 1. **CustomEmailOtpAuthenticator**
Implements the `Authenticator` interface:
- Validates email format.
- Generates and stores OTP in the session.
- Sends OTP via **EmailSenderService**.
- Displays an OTP input form for verification.
- Validates OTP with expiry & attempt checks.

### 2. **OtpHelper**
Handles OTP generation and session storage:
- Generates numeric OTP of configurable length.
- Stores OTP, expiry time, and attempt count in the authentication session.
- Validates OTP against stored value.
- Tracks and increments invalid attempts.

### 3. **CustomEmailOtpAuthenticatorFactory**
Registers the authenticator in Keycloak:
- Display name: `Email OTP Sender`.
- Provides configuration properties:
   - `Otp Length` → Number of digits. (5 default)
   - `Otp Expired` → Expiry time in minutes.(1 default)
   - `Max Attempt` → Maximum invalid attempts.(5 default)

** You can configure the above value in this email otp sender execution setting**

---

## Example: OTP Input Form

Here is the FreeMarker form (`otp-input.ftl`) displayed to the user:

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
