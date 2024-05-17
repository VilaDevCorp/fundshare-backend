package com.viladev.fundshare.exceptions;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class InactiveGroupException extends Exception {
    public InactiveGroupException(String message) {
        super(message);
    }
}