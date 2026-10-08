package com.pets.hall.web;

import com.pets.hall.model.Voice;
import com.pets.hall.service.ArchiveService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaController {
    private final ArchiveService archiveService;
    private final ResourceLoader resourceLoader;

    public SpaController(ArchiveService archiveService, ResourceLoader resourceLoader) {
        this.archiveService = archiveService;
        this.resourceLoader = resourceLoader;
    }

    @GetMapping({"/", "/login", "/register"})
    public void page(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Resource resource = resourceLoader.getResource("classpath:static/index.html");
        String html = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        Voice voice = archiveService.findVoice(request.getParameter("song")).orElse(null);
        if (voice != null) {
            html = SharePage.apply(html, SharePage.title(voice), SharePage.description(voice));
        }
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.TEXT_HTML_VALUE);
        response.setHeader("Cache-Control", "no-store");
        response.getWriter().write(html);
    }
}
