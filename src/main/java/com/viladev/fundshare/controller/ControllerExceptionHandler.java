package com.viladev.fundshare.controller;

import javax.management.InstanceNotFoundException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.viladev.fundshare.exceptions.EmptyFormFieldsException;
import com.viladev.fundshare.model.User;
import com.viladev.fundshare.utils.ApiResponse;

@ControllerAdvice
public class ControllerExceptionHandler {
    @ExceptionHandler(value = InstanceNotFoundException.class)
    public ResponseEntity<ApiResponse<User>> instanceNotFound(Exception e) {
        String errorMessage = e.getMessage() == null ? "Resource not found" : e.getMessage();
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiResponse<>(null, errorMessage));
    }

    @ExceptionHandler(value = EmptyFormFieldsException.class)
    public ResponseEntity<ApiResponse<User>> emptyFormField(Exception e) {
        String errorMessage = e.getMessage() == null ? "There are mandatory fields that are empty" : e.getMessage();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(null, errorMessage));
    }

    @ExceptionHandler(value = SecurityException.class)
    public ResponseEntity<ApiResponse<User>> notAllowedResourceError(Exception e) {
        String errorMessage = e.getMessage() == null ? "The user doesn't have permissions to do this operation"
                : e.getMessage();

        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ApiResponse<>(null, errorMessage));
    }

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<ApiResponse<User>> internalError(Exception e) {
        String errorMessage = e.getMessage() == null ? "It has occur an internal error" : e.getMessage();
        return ResponseEntity.internalServerError()
                .body(new ApiResponse<>(null, errorMessage));
    }

}
