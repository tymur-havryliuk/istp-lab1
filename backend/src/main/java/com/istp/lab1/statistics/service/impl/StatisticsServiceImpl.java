package com.istp.lab1.statistics.service.impl;

import com.istp.lab1.statistics.service.api.StatisticsService;
import com.istp.lab1.statistics.service.dto.CourseAverageGradeDto;
import com.istp.lab1.submission.dao.repository.SubmissionRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final SubmissionRepository submissionRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CourseAverageGradeDto> getAverageGradesByCourse() {
        return submissionRepository.findAverageGradesByCourse();
    }
}
