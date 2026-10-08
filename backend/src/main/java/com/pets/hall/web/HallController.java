package com.pets.hall.web;

import com.pets.hall.service.ArchiveService;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HallController {
    private final ArchiveService archiveService;

    public HallController(ArchiveService archiveService) {
        this.archiveService = archiveService;
    }

    @GetMapping("/api/archive")
    public Map<String, Object> archive() {
        return archiveService.load();
    }
}
