package com.template.backtemplate.exceptions;

public class ExpiredValidationCodeException extends Exception {
    public ExpiredValidationCodeException(String message) {
        super(message);
    }
}