package com.viladev.fundshare.exceptions;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class NonZeroBalanceException extends Exception {
    public NonZeroBalanceException(String message) {
        super(message);
    }
}