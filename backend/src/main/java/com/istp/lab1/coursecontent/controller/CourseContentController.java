package com.istp.lab1.coursecontent.controller;

import com.istp.lab1.coursecontent.controller.mapper.CourseContentMapper;
import com.istp.lab1.coursecontent.controller.request.CourseContentCreateRequest;
import com.istp.lab1.coursecontent.controller.response.CourseContentDeleteResponse;
import com.istp.lab1.coursecontent.controller.response.CourseContentResponse;
import com.istp.lab1.coursecontent.service.api.CourseContentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/courses/{courseId}/content")
@Tag(name = "Course Content", description = "Course lesson and content endpoints")
@RequiredArgsConstructor
public class CourseContentController {

    private final CourseContentService courseContentService;
    private final CourseContentMapper courseContentMapper;

    @GetMapping
    @Operation(summary = "Get course content")
    @PreAuthorize("isAuthenticated()")
    public List<CourseContentResponse> getCourseContent(@PathVariable Long courseId) {
        return courseContentMapper.toResponses(courseContentService.getCourseContent(courseId));
    }

    @PostMapping
    @Operation(summary = "Create course content")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<CourseContentResponse> createCourseContent(
            @PathVariable Long courseId,
            @Valid @RequestBody CourseContentCreateRequest body
    ) {
        var content = courseContentService.createCourseContent(courseId, courseContentMapper.toDto(body));
        return ResponseEntity.status(HttpStatus.CREATED).body(courseContentMapper.toResponse(content));
    }

    @DeleteMapping("/{contentId}")
    @Operation(summary = "Delete course content")
    @PreAuthorize("hasRole('TEACHER')")
    public CourseContentDeleteResponse deleteCourseContent(
            @PathVariable Long courseId,
            @PathVariable Long contentId
    ) {
        courseContentService.deleteCourseContent(courseId, contentId);
        return courseContentMapper.toDeleteResponse();
    }
}
