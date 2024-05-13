package com.viladev.fundshare.exceptions;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class KickedCreatorException extends Exception {
    public KickedCreatorException(String message) {
        super(message);
    }
}