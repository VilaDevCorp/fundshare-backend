package com.viladev.fundshare.exceptions;

import lombok.NoArgsConstructor;

@NoArgsConstructor  
public class UserKickedIsNotMember extends Exception {
    public UserKickedIsNotMember(String message) {
        super(message);
    }
}