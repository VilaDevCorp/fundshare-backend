package com.viladev.fundshare.utils;

import org.springframework.security.core.context.SecurityContextHolder;

import com.viladev.fundshare.exceptions.NotAllowedResourceException;
import com.viladev.fundshare.model.BaseEntity;
import com.viladev.fundshare.model.User;

public class AuthUtils {

    public static void checkIfCreator(BaseEntity entity) throws NotAllowedResourceException {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        if (entity.getCreatedBy().getUsername().equals(currentUsername)) {
            return;
        } else {
            throw new NotAllowedResourceException("You are not allowed to access this resource");
        }
    }

    public static boolean checkIfLoggedUser(User user) {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        return user.getUsername().equals(currentUsername);
    }

    public static String getUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

}
