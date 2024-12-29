package com.bervan.common.service;

import com.bervan.common.model.PersistableData;
import com.bervan.common.user.User;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collection;
import java.util.UUID;

public class AuthService {

    public static UUID getLoggedUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof String) {
            if (principal.equals("anonymousUser")) {
                return UUID.randomUUID();
            }
        }
        return ((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
    }

    public static boolean hasAccess(Collection<? extends PersistableData> elements) {
        if (elements == null || elements.size() == 0) {
            return false;
        }
        UUID loggedUserId = getLoggedUserId();

        return elements.stream().anyMatch(e -> e.getId().equals(loggedUserId));
    }
}
