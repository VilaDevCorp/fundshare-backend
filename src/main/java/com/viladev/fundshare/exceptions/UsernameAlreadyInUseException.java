package com.viladev.fundshare.exceptions;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class UsernameAlreadyInUseException extends Exception {
    public UsernameAlreadyInUseException(String message) {
        super(message);
    }
}