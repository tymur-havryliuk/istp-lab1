package com.istp.lab1.report.service.api;

import com.istp.lab1.report.service.dto.GradeImportSummaryDto;
import org.springframework.web.multipart.MultipartFile;

public interface ReportService {

    byte[] exportGradesReport();

    GradeImportSummaryDto importGradesReport(MultipartFile file);
}
