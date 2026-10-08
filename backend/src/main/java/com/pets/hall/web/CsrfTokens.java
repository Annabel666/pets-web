package com.pets.hall.web;

import com.pets.hall.model.SessionUser;
import jakarta.servlet.http.HttpSession;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class CsrfTokens {
    public static final String ATTRIBUTE = "csrfToken";
    public static final String ACCOUNT = "account";

    public String token(HttpSession session) {
        Object current = session.getAttribute(ATTRIBUTE);
        if (current instanceof String token && !token.isBlank()) {
            return token;
        }
        String token = UUID.randomUUID().toString();
        session.setAttribute(ATTRIBUTE, token);
        return token;
    }

    public boolean matches(HttpSession session, String provided) {
        Object current = session.getAttribute(ATTRIBUTE);
        return current instanceof String token && token.equals(provided);
    }

    public Optional<SessionUser> account(HttpSession session) {
        Object current = session.getAttribute(ACCOUNT);
        if (current instanceof SessionUser user) {
            return Optional.of(user);
        }
        return Optional.empty();
    }

    public void login(HttpSession session, SessionUser user) {
        session.setAttribute(ACCOUNT, user);
    }

    public void logout(HttpSession session) {
        session.invalidate();
    }
}
