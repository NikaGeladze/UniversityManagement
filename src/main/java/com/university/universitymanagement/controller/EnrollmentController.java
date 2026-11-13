package com.university.universitymanagement.controller;

import com.university.universitymanagement.dto.request.EnrollmentRequest;
import com.university.universitymanagement.dto.response.EnrollmentResponse;
import com.university.universitymanagement.dto.response.PageResponse;
import com.university.universitymanagement.entity.EnrollmentStatus;
import com.university.universitymanagement.service.EnrollmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/enrollments")
@Tag(name = "Enrollments", description = "Enroll students in courses and cancel enrollments")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    public EnrollmentController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    @GetMapping
    @Operation(summary = "List enrollments", description = "Optionally filter by student, course and status.")
    public PageResponse<EnrollmentResponse> getAll(
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) EnrollmentStatus status,
            @ParameterObject @PageableDefault(size = 10, sort = "enrollmentDate", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return enrollmentService.getAll(studentId, courseId, status, pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get an enrollment by id")
    public EnrollmentResponse getById(@PathVariable Long id) {
        return enrollmentService.getById(id);
    }

    @PostMapping
    @Operation(summary = "Enroll a student in a course",
            description = "Fails with 409 if the student is already enrolled or the course is full.")
    public ResponseEntity<EnrollmentResponse> enroll(@Valid @RequestBody EnrollmentRequest request) {
        EnrollmentResponse created = enrollmentService.enroll(request);
        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/enrollments/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}/cancel")
    @Operation(summary = "Cancel an enrollment")
    public EnrollmentResponse cancel(@PathVariable Long id) {
        return enrollmentService.cancel(id);
    }
}
