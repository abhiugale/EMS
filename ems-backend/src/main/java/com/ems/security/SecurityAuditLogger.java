package com.ems.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SecurityAuditLogger {
    private static final Logger log = LoggerFactory.getLogger(SecurityAuditLogger.class);

    public void logLoginSuccess(String email) {
        log.info("[SECURITY AUDIT] Successful login for user: {}", email);
    }

    public void logLoginFailure(String email, String reason) {
        log.warn("[SECURITY AUDIT] Failed login attempt for user: {}. Reason: {}", email, reason);
    }

    public void logRegistration(String email, String role, String registeredBy) {
        log.info("[SECURITY AUDIT] User {} registered new user: {} with role: {}", registeredBy, email, role);
    }

    public void logLogout(String email) {
        log.info("[SECURITY AUDIT] User logged out: {}", email);
    }

    public void logUnauthorizedAccess(String email, String uri, String reason) {
        log.warn("[SECURITY AUDIT] Unauthorized access attempt by user: {} on URI: {}. Reason: {}", email, uri, reason);
    }
}
