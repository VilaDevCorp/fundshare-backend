package com.viladev.fundshare.exceptions;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ExpiredValidationCodeException extends Exception {
    public ExpiredValidationCodeException(String message) {
        super(message);
    }
}