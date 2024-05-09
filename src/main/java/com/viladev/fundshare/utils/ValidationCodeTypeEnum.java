package com.viladev.fundshare.utils;

public enum ValidationCodeTypeEnum {
    ACTIVATE_ACCOUNT("ACTIVATE_ACCOUNT"), RESET_PASSWORD("RESET_PASSWORD");

    private String type;

    ValidationCodeTypeEnum(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}