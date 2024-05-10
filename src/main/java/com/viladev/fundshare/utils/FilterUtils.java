package com.viladev.fundshare.utils;


import com.viladev.fundshare.model.BaseEntity;

public class FilterUtils {

    public static void checkIfCreator(BaseEntity entity, String username) {

        if (entity.getCreatedBy().getUsername().equals(username)) {
            return;
        } else {
            throw new SecurityException("You are not allowed to access this resource");
        }
    }
}
