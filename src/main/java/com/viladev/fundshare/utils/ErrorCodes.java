package com.viladev.fundshare.utils;

public class ErrorCodes {

    private ErrorCodes() {
    }

    public static final String NOT_JWT_TOKEN = "NOT_JWT_TOKEN";
    public static final String NOT_CSRF_TOKEN = "NOT_CSR_TOKEN";
    public static final String INVALID_TOKEN = "INVALID_TOKEN";
    public static final String NOT_VALIDATED_ACCOUNT = "NOT_VALIDATED_ACCOUNT";

    public static final String USERNAME_ALREADY_IN_USE = "USERNAME_ALREADY_IN_USE";
    public static final String EMAIL_ALREADY_IN_USE = "EMAIL_ALREADY_IN_USE";
    public static final String ALREADY_VALIDATED_ACCOUNT = "ALREADY_VALIDATED_ACCOUNT";
    public static final String INVALID_CREDENTIALS = "INVALID_CREDENTIALS";
    public static final String EXPIRED_VALIDATION_CODE = "EXPIRED_VALIDATION_CODE";
    public static final String INCORRECT_VALIDATION_CODE = "INCORRECT_VALIDATION_CODE";
    public static final String ALREADY_USED_VALIDATION_CODE = "ALREADY_USED_VALIDATION_CODE";
    public static final String ALREADY_INVITED_USER = "ALREADY_INVITED_USER";
    public static final String ALREADY_MEMBER_GROUP = "ALREADY_MEMBER_GROUP";
    public static final String NOT_GROUP_MEMBER = "NOT_GROUP_MEMBER";
    public static final String KICKED_CREATOR = "KICKED_CREATOR";
    public static final String NOT_ABOVE_0_AMOUNT = "NOT_ABOVE_0_AMOUNT";
    public static final String PAYEE_NOT_IN_GROUP = " PAYEE_NOT_IN_GROUP";
    public static final String PAYER_NOT_IN_GROUP = "PAYER_NOT_IN_GROUP";
    public static final String CLOSED_GROUP = "INACTIVE_GROUP";
    public static final String NON_ZERO_BALANCE = "NON_ZERO_BALANCE";

}
