package com.istp.lab1.report.service.impl;

import com.istp.lab1.exception.BadRequestException;
import com.istp.lab1.exception.ForbiddenException;
import com.istp.lab1.report.service.api.ReportService;
import com.istp.lab1.report.service.dto.GradeImportSummaryDto;
import com.istp.lab1.security.CurrentUser;
import com.istp.lab1.security.CurrentUserResolver;
import com.istp.lab1.submission.dao.entity.SubmissionEntity;
import com.istp.lab1.submission.dao.repository.SubmissionRepository;
import com.istp.lab1.user.dao.entity.UserRole;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private static final String TEACHER_ROLE_REQUIRED = "Teacher role is required";
    private static final String INVALID_REPORT_MESSAGE = "Invalid grades report file";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final SubmissionRepository submissionRepository;
    private final CurrentUserResolver currentUserResolver;

    @Override
    @Transactional(readOnly = true)
    public byte[] exportGradesReport() {
        CurrentUser currentUser = currentUser();
        requireTeacher(currentUser);
        List<SubmissionEntity> submissions = submissionRepository.findGradedByTeacherUserId(currentUser.id());

        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Grades");
            createHeader(sheet.createRow(0));

            int rowIndex = 1;
            for (SubmissionEntity submission : submissions) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(submission.getAssignment().getCourse().getTitle());
                row.createCell(1).setCellValue(submission.getAssignment().getTitle());
                row.createCell(2).setCellValue(submission.getStudent().getUser().getFullName());
                row.createCell(3).setCellValue(submission.getScore());
                row.createCell(4).setCellValue(submission.getFeedback() == null ? "" : submission.getFeedback());
                row.createCell(5).setCellValue(submission.getGradedAt() == null
                        ? ""
                        : DATE_TIME_FORMATTER.format(submission.getGradedAt()));
            }

            for (int i = 0; i < 6; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (IOException exception) {
            throw new BadRequestException("Could not generate grades report");
        }
    }

    @Override
    public GradeImportSummaryDto importGradesReport(MultipartFile file) {
        CurrentUser currentUser = currentUser();
        requireTeacher(currentUser);
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File must not be empty");
        }

        try (XSSFWorkbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getNumberOfSheets() == 0 ? null : workbook.getSheetAt(0);
            if (sheet == null) {
                return new GradeImportSummaryDto(0, null, null, null, 0);
            }

            DataFormatter formatter = new DataFormatter();
            int totalRows = 0;
            int gradeCount = 0;
            int gradeSum = 0;
            Integer minGrade = null;
            Integer maxGrade = null;
            Set<String> courseTitles = new HashSet<>();

            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null || isEmpty(row, formatter)) {
                    continue;
                }

                totalRows++;
                String courseTitle = formatter.formatCellValue(row.getCell(0)).trim();
                if (!courseTitle.isEmpty()) {
                    courseTitles.add(courseTitle);
                }

                Integer grade = readGrade(row.getCell(3), formatter);
                if (grade != null) {
                    gradeCount++;
                    gradeSum += grade;
                    minGrade = minGrade == null ? grade : Math.min(minGrade, grade);
                    maxGrade = maxGrade == null ? grade : Math.max(maxGrade, grade);
                }
            }

            Double averageGrade = gradeCount == 0 ? null : (double) gradeSum / gradeCount;
            return new GradeImportSummaryDto(totalRows, averageGrade, minGrade, maxGrade, courseTitles.size());
        } catch (IOException | NumberFormatException exception) {
            throw new BadRequestException(INVALID_REPORT_MESSAGE);
        }
    }

    private void createHeader(Row row) {
        row.createCell(0).setCellValue("Course");
        row.createCell(1).setCellValue("Assignment");
        row.createCell(2).setCellValue("Student");
        row.createCell(3).setCellValue("Grade");
        row.createCell(4).setCellValue("Feedback");
        row.createCell(5).setCellValue("Graded At");
    }

    private Integer readGrade(Cell cell, DataFormatter formatter) {
        if (cell == null) {
            return null;
        }
        String value = formatter.formatCellValue(cell).trim();
        if (value.isEmpty()) {
            return null;
        }
        return Integer.valueOf(value.contains(".") ? value.substring(0, value.indexOf('.')) : value);
    }

    private boolean isEmpty(Row row, DataFormatter formatter) {
        for (int i = 0; i < 6; i++) {
            if (!formatter.formatCellValue(row.getCell(i)).trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private void requireTeacher(CurrentUser currentUser) {
        if (currentUser.role() != UserRole.TEACHER) {
            throw new ForbiddenException(TEACHER_ROLE_REQUIRED);
        }
    }

    private CurrentUser currentUser() {
        return currentUserResolver.resolveCurrentUser();
    }
}
