package com.viladev.fundshare.exceptions;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class FileTooBigException extends Exception {
    public FileTooBigException(String message) {
        super(message);
    }
}