package com.pets.hall.web;

import com.pets.hall.model.SessionUser;
import com.pets.hall.service.GuestbookService;
import jakarta.servlet.http.HttpSession;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/messages")
public class MessageController {
    private final GuestbookService guestbookService;
    private final CsrfTokens csrfTokens;
    private final VisitLimits visitLimits;

    public MessageController(GuestbookService guestbookService, CsrfTokens csrfTokens, VisitLimits visitLimits) {
        this.guestbookService = guestbookService;
        this.csrfTokens = csrfTokens;
        this.visitLimits = visitLimits;
    }

    @GetMapping
    public Map<String, Object> latest(@RequestParam(required = false) Long before, HttpSession session) {
        return guestbookService.latest(csrfTokens.account(session).orElse(null), before);
    }

    @GetMapping("/mine")
    public ResponseEntity<?> mine(@RequestParam(required = false) Long before, HttpSession session) {
        Optional<SessionUser> account = csrfTokens.account(session);
        if (account.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "登录后才能查看自己写过的话。"));
        }
        return ResponseEntity.ok(guestbookService.mine(account.get(), before));
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> post(
            @RequestBody Map<String, Object> body,
            @RequestHeader(value = "X-CSRF-Token", defaultValue = "") String csrf,
            HttpSession session) {
        if (!csrfTokens.matches(session, csrf)) {
            return ResponseEntity.badRequest().body(Map.of("error", "页面已过期，请再试一次。"));
        }
        Optional<SessionUser> account = csrfTokens.account(session);
        if (account.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "登录后才能留言。"));
        }
        String postKey = "post:" + account.get().getId();
        if (!visitLimits.allow(postKey, 20, java.time.Duration.ofMinutes(10))) {
            return ResponseEntity.badRequest().body(Map.of("error", "写得太勤了，稍后再留一句。"));
        }
        String message = guestbookService.post(account.get(), text(body.get("content")), parentId(body.get("parentId")));
        if (!message.isEmpty()) {
            visitLimits.undo(postKey);
            return ResponseEntity.badRequest().body(Map.of("error", message));
        }
        return ResponseEntity.ok(new LinkedHashMap<>());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, String>> update(
            @PathVariable long id,
            @RequestBody Map<String, Object> body,
            @RequestHeader(value = "X-CSRF-Token", defaultValue = "") String csrf,
            HttpSession session) {
        if (!csrfTokens.matches(session, csrf)) {
            return ResponseEntity.badRequest().body(Map.of("error", "页面已过期，请再试一次。"));
        }
        String message = guestbookService.update(csrfTokens.account(session).orElse(null), id, text(body.get("content")));
        if (!message.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", message));
        }
        return ResponseEntity.ok(new LinkedHashMap<>());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> remove(
            @PathVariable long id,
            @RequestHeader(value = "X-CSRF-Token", defaultValue = "") String csrf,
            HttpSession session) {
        if (!csrfTokens.matches(session, csrf)) {
            return ResponseEntity.badRequest().body(Map.of("error", "页面已过期，请再试一次。"));
        }
        String message = guestbookService.remove(csrfTokens.account(session).orElse(null), id);
        if (!message.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", message));
        }
        return ResponseEntity.ok(new LinkedHashMap<>());
    }

    @PostMapping("/{id}/agree")
    public ResponseEntity<Map<String, String>> agree(
            @PathVariable long id,
            @RequestHeader(value = "X-CSRF-Token", defaultValue = "") String csrf,
            HttpSession session) {
        if (!csrfTokens.matches(session, csrf)) {
            return ResponseEntity.badRequest().body(Map.of("error", "页面已过期，请再试一次。"));
        }
        SessionUser account = csrfTokens.account(session).orElse(null);
        String agreeKey = account == null ? "" : "agree:" + account.getId();
        if (account != null && !visitLimits.allow(agreeKey, 60, java.time.Duration.ofMinutes(1))) {
            return ResponseEntity.badRequest().body(Map.of("error", "点得太勤了，稍后再试。"));
        }
        String message = guestbookService.toggleAgree(account, id);
        if (!message.isEmpty() && account != null) {
            visitLimits.undo(agreeKey);
        }
        if (!message.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", message));
        }
        return ResponseEntity.ok(new LinkedHashMap<>());
    }

    private static String text(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private static Long parentId(Object value) {
        if (value == null) {
            return null;
        }
        String raw = String.valueOf(value).trim();
        if (raw.isEmpty() || "null".equals(raw)) {
            return null;
        }
        try {
            return Long.valueOf(raw);
        } catch (NumberFormatException ex) {
            return -1L;
        }
    }
}
