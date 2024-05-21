package com.viladev.fundshare.exceptions;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class UserAlreadyValidatedException extends Exception {
    public UserAlreadyValidatedException(String message) {
        super(message);
    }
}