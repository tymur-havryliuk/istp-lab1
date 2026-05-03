package com.istp.lab1.grade.service.api;

import com.istp.lab1.grade.service.dto.GradeDto;
import com.istp.lab1.grade.service.dto.GradeSaveDto;
import com.istp.lab1.submission.service.dto.SubmissionDto;
import java.util.List;

public interface GradeService {

    SubmissionDto gradeSubmission(Long submissionId, GradeSaveDto grade);

    List<GradeDto> getStudentGrades(Long studentId);
}
