package com.bervan.common.service;

import com.bervan.common.model.PersistableData;
import com.bervan.common.user.User;
import com.bervan.common.user.UserToUserRelation;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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

        User loggedUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!loggedUser.isMainAccount()) {
            Set<User> parents = loggedUser.getChildrenRelations().stream().filter(e -> e.getChild().getId().equals(loggedUser.getId()))
                    .map(UserToUserRelation::getParent)
                    .collect(Collectors.toSet());

            for (User parent : parents) {
                if (elements.stream().anyMatch(e -> e.getId().equals(parent.getId()))) {
                    return true;
                }
            }
        }
        return elements.stream().anyMatch(e -> e.getId().equals(loggedUser.getId()));
    }

    public static String getUserRole() {
        User principal = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return principal.getRole();
    }
}
