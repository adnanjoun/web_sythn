package com.syntheaweb.backend.controller;

import com.syntheaweb.backend.service.OmopExportService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/omop")
public class OmopExportController {

    private final OmopExportService omopExportService;

    public OmopExportController(OmopExportService omopExportService) {
        this.omopExportService = omopExportService;
    }

    @GetMapping("/download")
    public void downloadOmop(@RequestParam String runId,
                             HttpServletResponse response) throws IOException {

        omopExportService.exportRun(runId, response);
    }
}
