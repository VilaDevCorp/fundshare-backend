package com.viladev.fundshare.exceptions;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class PayerIsNotInGroupException extends Exception {
    public PayerIsNotInGroupException(String message) {
        super(message);
    }
}