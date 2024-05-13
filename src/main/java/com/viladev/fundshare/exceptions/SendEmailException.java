package com.viladev.fundshare.exceptions;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class SendEmailException extends Exception {
    public SendEmailException(String message) {
        super(message);
    }
}