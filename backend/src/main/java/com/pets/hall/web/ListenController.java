package com.pets.hall.web;

import com.pets.hall.model.SessionUser;
import com.pets.hall.service.ListenService;
import jakarta.servlet.http.HttpSession;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/listens")
public class ListenController {
    private final ListenService listenService;
    private final CsrfTokens csrfTokens;

    public ListenController(ListenService listenService, CsrfTokens csrfTokens) {
        this.listenService = listenService;
        this.csrfTokens = csrfTokens;
    }

    @GetMapping
    public Map<String, Object> list(HttpSession session) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("ids", idsFor(csrfTokens.account(session).orElse(null)));
        return body;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> remember(
            @RequestBody Map<String, String> body,
            @RequestHeader(value = "X-CSRF-Token", defaultValue = "") String csrf,
            HttpSession session) {
        if (!csrfTokens.matches(session, csrf)) {
            return ResponseEntity.badRequest().body(Map.of("error", "页面已过期，请再试一次。"));
        }
        SessionUser account = csrfTokens.account(session).orElse(null);
        if (account == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "登录后才能记下听过的歌。"));
        }
        String voiceId = body.get("voiceId");
        if (!listenService.remember(account.getId(), voiceId)) {
            return ResponseEntity.badRequest().body(Map.of("error", "这首没有记下。"));
        }
        Map<String, Object> ok = new LinkedHashMap<>();
        ok.put("ids", listenService.recent(account.getId()));
        return ResponseEntity.ok(ok);
    }

    private List<String> idsFor(SessionUser account) {
        if (account == null || account.getId() == null) {
            return List.of();
        }
        return listenService.recent(account.getId());
    }
}
