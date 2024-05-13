package com.viladev.fundshare.exceptions;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class NotAbove0AmountException extends Exception {
    public NotAbove0AmountException(String message) {
        super(message);
    }
}