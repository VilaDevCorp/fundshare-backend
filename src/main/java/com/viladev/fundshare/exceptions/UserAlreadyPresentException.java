package com.viladev.fundshare.exceptions;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class UserAlreadyPresentException extends Exception {
    public UserAlreadyPresentException(String message) {
        super(message);
    }
}