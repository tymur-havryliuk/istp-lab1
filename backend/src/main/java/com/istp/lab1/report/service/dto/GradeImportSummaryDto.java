package com.istp.lab1.report.service.dto;

public record GradeImportSummaryDto(
        int totalRows,
        Double averageGrade,
        Integer minGrade,
        Integer maxGrade,
        int courseCount
) {
}
