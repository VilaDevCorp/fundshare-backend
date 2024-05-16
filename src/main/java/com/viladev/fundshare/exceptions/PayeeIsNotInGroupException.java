package com.viladev.fundshare.exceptions;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class PayeeIsNotInGroupException extends Exception {
    public PayeeIsNotInGroupException(String message) {
        super(message);
    }
}