package com.viladev.fundshare.exceptions;

public class UserAlreadyInvitedException extends Exception {
    public UserAlreadyInvitedException(String message) {
        super(message);
    }
}