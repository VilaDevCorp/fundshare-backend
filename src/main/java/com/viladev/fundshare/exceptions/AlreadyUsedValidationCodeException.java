package com.viladev.fundshare.exceptions;

public class AlreadyUsedValidationCodeException extends Exception {
    public AlreadyUsedValidationCodeException(String message) {
        super(message);
    }
}