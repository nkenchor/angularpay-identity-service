package io.angularpay.identity.exceptions;

import lombok.Getter;

@Getter
public enum ErrorCode {
    INVALID_MESSAGE_ERROR("The message format read from the given topic is invalid"),
    VALIDATION_ERROR("The request has validation errors"),
    RESTRICTED_ROLE_ERROR("Your request contains one or more restricted roles"),
    ILLEGAL_SELF_ROLE_ASSIGNMENT_ERROR("Self role assignment is NOT permitted"),
    ILLEGAL_REQUEST_ERROR("You cannot performed this action on a request that has NOT been confirmed"),
    REQUEST_NOT_FOUND("The requested resource was NOT found"),
    OTP_SERVICE_ERROR("Unable to generate OTP. Please check otp-service logs for details"),
    NOTIFICATION_SERVICE_ERROR("Unable to send notification. Please check notification-service logs for details."),
    USERCONFIG_SERVICE_ERROR("Unable to create User Configuration Account. Please check userconfig-service logs for details."),
    INVALID_OTP_ERROR("The OTP you entered is invalid."),
    DUPLICATE_REQUEST_ERROR("A resource having the same identifier already exist"),
    INVALID_USER_STATUS_ERROR("You cannot performed this action on a user resource that is inactive"),
    UNRECOGNIZED_DEVICE_ERROR("Login from an unregistered device is NOT allowed"),
    USER_DEVICE_MISMATCH_ERROR("Accessing your account from an unregistered device is NOT allowed"),
    INVALID_CREDENTIALS("The access credentials provided is invalid"),
    CIPHER_SERVICE_ERROR("Unable to generate cipher. Please check cipher-service logs for details"),
    TOKEN_GENERATION_ERROR("Unable to generate access token. Please ensure that the RSA keys are valid"),
    GENERIC_ERROR("Generic error occurred. See stacktrace for details"),
    BLOCKED_IP_ERROR("This IP has been blocked!"),
    MISSING_IP_ERROR("There is no IP to identify the remote client!"),
    AUTHORIZATION_ERROR("You do NOT have adequate permission to access this resource"),
    NO_PRINCIPAL("Principal identifier NOT provided", 500);

    private final String defaultMessage;
    private final int defaultHttpStatus;

    ErrorCode(String defaultMessage) {
        this(defaultMessage, 400);
    }

    ErrorCode(String defaultMessage, int defaultHttpStatus) {
        this.defaultMessage = defaultMessage;
        this.defaultHttpStatus = defaultHttpStatus;
    }
}
