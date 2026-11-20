package com.expensemanagement.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {

    private SecurityUtils() {
    }

    public static UserPrincipal getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal) {
            return (UserPrincipal) authentication.getPrincipal();
        }
        return null;
    }

    public static Long getCurrentUserId() {
        UserPrincipal principal = getCurrentUser();
        return principal != null ? principal.getId() : null;
    }

    public static String getCurrentUserEmail() {
        UserPrincipal principal = getCurrentUser();
        return principal != null ? principal.getUsername() : null;
    }

    public static boolean hasRole(String roleName) {
        UserPrincipal principal = getCurrentUser();
        if (principal == null) return false;
        return principal.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals(roleName));
    }
}
