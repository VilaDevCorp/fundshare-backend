package com.viladev.fundshare.exceptions;

public class UserAlreadyPresentException extends Exception {
    public UserAlreadyPresentException(String message) {
        super(message);
    }
}