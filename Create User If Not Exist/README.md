# Custom Create User SPI for Keycloak

## Overview

This custom Keycloak SPI (Service Provider Interface) is designed to handle user authentication by checking if a user exists in the Keycloak realm. If the user does not exist, a new user is created automatically. This is particularly useful when integrating custom login flows where users might log in with usernames (such as email, phone number, or regular username) that are not pre-registered in Keycloak.

## Features
- **Create User Automatically**: If the user doesn't exist in Keycloak, it will create a new user using the provided username.
- **Enable Disabled Users**: If the user is found but is disabled, the authenticator will re-enable the user.

## Components
The SPI consists of two main classes:
1. **CustomCreateUserAuthenticator**: This class contains the logic to authenticate the user, check if they exist, and create the user if necessary.
2. **CustomCreateUserAuthenticatorFactory**: This factory class registers the custom authenticator with Keycloak.

## Installation

1. **Clone the repository**:
   Clone this repository to your local machine or server where you are running your Keycloak instance.

   ```bash
   git clone https://github.com/your-repo/custom-create-user-spi.git
   cd custom-create-user-spi

2. **Build the JAR file**:
   Build the SPI as a JAR file using Maven or Gradle. Example using Maven:
     ```bash
   mvn clean install
3. **Deploy to Keycloak:**
  Copy the generated JAR file into the /providers directory of your Keycloak server.
4. **Restart Keycloak**:
   Restart your Keycloak server to load the new SPI.
    ```bash
   ./kc.sh build
   ./kc.sh start-dev
## Configuration

To configure the **CustomCreateUserAuthenticator** SPI, follow these steps:

### 1. Create an Authentication Flow

1. In the **Keycloak Admin Console**, navigate to **Authentication**.
2. Create a new flow or edit an existing one.
3. Add the **Create User If Not Exist** authenticator to the flow.


## Usage

### Authentication Flow

1. The user provides their **username** (which can be an **email**, **phone number**, or **normal username**).
2. The custom authenticator checks if the user exists in the **Keycloak realm**:
    - **If the user exists and is enabled**, authentication proceeds as normal.
    - **If the user does not exist**, a new user is created with the provided username and is enabled by default.
3. After **user creation** or **re-enabling**, the user is authenticated, and the flow continues.


### Notes:
1. **Customization**: You can customize  the logic based on your requirements.
2. **Keycloak Integration**: Make sure you understand how to deploy and configure Keycloak with custom SPIs. You may need to restart your Keycloak instance after deploying the JAR.
