package com.viladev.fundshare.exceptions;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class UserAlreadyInvitedException extends Exception {
    public UserAlreadyInvitedException(String message) {
        super(message);
    }
}