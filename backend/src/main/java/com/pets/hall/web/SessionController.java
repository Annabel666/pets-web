package com.pets.hall.web;

import com.pets.hall.model.HallUser;
import com.pets.hall.model.SessionUser;
import com.pets.hall.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class SessionController {
    private final AuthService authService;
    private final CsrfTokens csrfTokens;
    private final VisitLimits visitLimits;

    public SessionController(AuthService authService, CsrfTokens csrfTokens, VisitLimits visitLimits) {
        this.authService = authService;
        this.csrfTokens = csrfTokens;
        this.visitLimits = visitLimits;
    }

    @GetMapping("/session")
    public Map<String, String> session(HttpSession session) {
        Map<String, String> body = new LinkedHashMap<>();
        body.put("csrfToken", csrfTokens.token(session));
        csrfTokens.account(session).ifPresent(user -> body.put("username", user.getUsername()));
        return body;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(
            @RequestBody Map<String, String> body,
            @RequestHeader(value = "X-CSRF-Token", defaultValue = "") String csrf,
            HttpServletRequest request,
            HttpSession session) {
        if (!csrfTokens.matches(session, csrf)) {
            return error("页面已过期，请再试一次。");
        }
        String name = body.get("username") == null ? "" : body.get("username").trim().toLowerCase(Locale.ROOT);
        if (!visitLimits.allow("login-ip:" + request.getRemoteAddr(), 40, Duration.ofMinutes(15))) {
            return error("试得太勤了，请稍后再来。");
        }
        Optional<HallUser> user = authService.login(body.get("username"), body.get("password"));
        if (user.isEmpty()) {
            if (!visitLimits.allow("login-name:" + name, 10, Duration.ofMinutes(15))) {
                return error("试得太勤了，请稍后再来。");
            }
            return error("用户名或密码不正确。");
        }
        HallUser account = user.get();
        csrfTokens.login(session, new SessionUser(account.getId(), account.getUsername()));
        Map<String, String> ok = new LinkedHashMap<>();
        ok.put("username", account.getUsername());
        return ResponseEntity.ok(ok);
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(
            @RequestBody Map<String, String> body,
            @RequestHeader(value = "X-CSRF-Token", defaultValue = "") String csrf,
            HttpServletRequest request,
            HttpSession session) {
        if (!csrfTokens.matches(session, csrf)) {
            return error("页面已过期，请再试一次。");
        }
        String registerKey = "register:" + request.getRemoteAddr();
        if (!visitLimits.allow(registerKey, 5, Duration.ofHours(1))) {
            return error("注册太勤了，请稍后再来。");
        }
        String message = authService.register(body.get("username"), body.get("password"), body.get("confirm"));
        if (!message.isEmpty()) {
            visitLimits.undo(registerKey);
            return error(message);
        }
        return ResponseEntity.ok(Map.of("notice", "注册成功，登录后就可以留言。"));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(
            @RequestHeader(value = "X-CSRF-Token", defaultValue = "") String csrf, HttpSession session) {
        if (!csrfTokens.matches(session, csrf)) {
            return error("页面已过期，请再试一次。");
        }
        csrfTokens.logout(session);
        return ResponseEntity.ok(new LinkedHashMap<>());
    }

    private static ResponseEntity<Map<String, String>> error(String message) {
        return ResponseEntity.badRequest().body(Map.of("error", message));
    }
}
