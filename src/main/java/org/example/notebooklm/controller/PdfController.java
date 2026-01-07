package org.example.notebooklm.controller;

import org.example.notebooklm.service.DemoAnswerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class PdfController {

    private static final Logger logger = LoggerFactory.getLogger(PdfController.class);

    private final DemoAnswerService demoService;

    // שמירת הקובץ האחרון שהועלה (ל־demo בלבד)
    private String lastUploadedFilename;

    public PdfController(DemoAnswerService demoService) {
        this.demoService = demoService;
    }

    // =========================
    // Upload PDF
    // =========================
    @PostMapping("/upload")
    public Map<String, String> uploadPdf(@RequestParam("file") MultipartFile file) {

        Map<String, String> resp = new HashMap<>();

        if (file.isEmpty()) {
            resp.put("message", "No file selected");
            return resp;
        }

        lastUploadedFilename = file.getOriginalFilename();

        logger.info("PDF uploaded: {}", lastUploadedFilename);

        resp.put("message", "PDF uploaded successfully: " + lastUploadedFilename);
        return resp;
    }

    // =========================
    // Ask Question
    // =========================
    @PostMapping("/question")
    public Map<String, Object> askQuestion(@RequestBody Map<String, String> request) {

        String question = request.get("question");

        Long pdfId = resolvePdfId(lastUploadedFilename);

        logger.info("Resolved pdfId={} for file={}", pdfId, lastUploadedFilename);

        String answer = demoService.getAnswer(pdfId, question);

        Map<String, Object> resp = new HashMap<>();
        resp.put("answer", answer);
        resp.put("pdfId", pdfId);
        resp.put("chunkNumber", 0);

        return resp;
    }

    // =========================
    // PDF Identification Logic
    // =========================
    private Long resolvePdfId(String filename) {

        if (filename == null) {
            return 1L; // default CV
        }

        String name = filename.toLowerCase();

        if (name.contains("harry")) {
            return 2L;
        }

        if (name.contains("cv") || name.contains("resume")) {
            return 1L;
        }

        // default fallback
        return 1L;
    }
}
