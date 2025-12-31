package com.xenplan.app.security;

import com.xenplan.app.domain.entity.User;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {

    private SecurityUtils() {
        // Util methods only
    }

    /**
     * Get the currently authenticated user.
     */
    public static User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null 
                && !(authentication instanceof AnonymousAuthenticationToken) 
                && authentication.isAuthenticated()) {
            
            Object principal = authentication.getPrincipal();
            if (principal instanceof UserDetailsImpl) {
                return ((UserDetailsImpl) principal).getUser();
            }
        }
        return null;
    }

    /**
     * Check if the current user is logged in.
     */
    public static boolean isUserLoggedIn() {
        return getCurrentUser() != null;
    }

    /**
     * Check if the current user is authenticated.
     * (Added this method to fix the compilation error in Login/Register views)
     */
    public static boolean isAuthenticated() {
        return isUserLoggedIn();
    }

    /**
     * Check if the current user has a specific role.
     * @param role The role to check (e.g., "ADMIN", "CLIENT")
     */
    public static boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null) {
            return false;
        }

        // Spring Security roles usually start with "ROLE_"
        String roleToCheck = "ROLE_" + role;

        return authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals(roleToCheck));
    }
    
    /**
     * Logout the current user.
     */
    public static void logout() {
        SecurityContextHolder.clearContext();
    }
}