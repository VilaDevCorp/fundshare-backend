package com.viladev.fundshare.exceptions;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class FileTypeNotSupportedException extends Exception {
    public FileTypeNotSupportedException(String message) {
        super(message);
    }
}