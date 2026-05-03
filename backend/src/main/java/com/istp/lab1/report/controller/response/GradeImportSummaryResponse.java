package com.istp.lab1.report.controller.response;

import com.istp.lab1.report.service.dto.GradeImportSummaryDto;

public record GradeImportSummaryResponse(
        int totalRows,
        Double averageGrade,
        Integer minGrade,
        Integer maxGrade,
        int courseCount
) {

    public static GradeImportSummaryResponse fromDto(GradeImportSummaryDto dto) {
        return new GradeImportSummaryResponse(
                dto.totalRows(),
                dto.averageGrade(),
                dto.minGrade(),
                dto.maxGrade(),
                dto.courseCount()
        );
    }
}
