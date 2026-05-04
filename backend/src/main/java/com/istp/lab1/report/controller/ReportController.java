package com.istp.lab1.report.controller;

import com.istp.lab1.report.controller.response.GradeImportSummaryResponse;
import com.istp.lab1.report.service.api.ReportService;
import com.istp.lab1.security.CurrentUserResolver;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    private final CurrentUserResolver currentUserResolver;

    @GetMapping("/grades/export")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<byte[]> exportGradesReport(HttpServletRequest request) {
        byte[] report = reportService.exportGradesReport(currentUserResolver.resolve(request));
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                ))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename("grades-report.xlsx")
                        .build()
                        .toString())
                .body(report);
    }

    @PostMapping(value = "/grades/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('TEACHER')")
    public GradeImportSummaryResponse importGradesReport(
            HttpServletRequest request,
            @RequestParam("file") MultipartFile file
    ) {
        return GradeImportSummaryResponse.fromDto(
                reportService.importGradesReport(currentUserResolver.resolve(request), file)
        );
    }
}
