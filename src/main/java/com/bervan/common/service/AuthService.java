package com.bervan.common.service;

import com.bervan.common.user.User;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

public class AuthService {

    public static UUID getLoggedUserId() {
        return ((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
    }
}
