package com.viladev.fundshare.exceptions;

public class ExpiredValidationCodeException extends Exception {
    public ExpiredValidationCodeException(String message) {
        super(message);
    }
}