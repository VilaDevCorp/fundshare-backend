package com.viladev.fundshare.exceptions;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class EmptyFormFieldsException extends Exception {
    public EmptyFormFieldsException(String message) {
        super(message);
    }
}