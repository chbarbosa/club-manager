package com.clubmanager.dto;

import java.util.List;
import java.util.UUID;

public record AuthenticatedUser(String username, UUID uuid, String name, String role, List<String> availableRoles) {

    public boolean multipleRoles() {
        return availableRoles.size() > 1;
    }
}
