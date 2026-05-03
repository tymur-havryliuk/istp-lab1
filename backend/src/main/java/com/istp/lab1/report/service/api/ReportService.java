package com.istp.lab1.report.service.api;

import com.istp.lab1.report.service.dto.GradeImportSummaryDto;
import com.istp.lab1.security.CurrentUser;
import org.springframework.web.multipart.MultipartFile;

public interface ReportService {

    byte[] exportGradesReport(CurrentUser currentUser);

    GradeImportSummaryDto importGradesReport(CurrentUser currentUser, MultipartFile file);
}
