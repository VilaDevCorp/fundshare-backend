package com.viladev.fundshare.exceptions;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class AlreadyUsedValidationCodeException extends Exception {
    public AlreadyUsedValidationCodeException(String message) {
        super(message);
    }
}