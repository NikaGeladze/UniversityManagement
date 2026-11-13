package com.university.universitymanagement.controller;

import com.university.universitymanagement.dto.request.CourseRequest;
import com.university.universitymanagement.dto.response.CoursePopularityResponse;
import com.university.universitymanagement.dto.response.CourseResponse;
import com.university.universitymanagement.dto.response.PageResponse;
import com.university.universitymanagement.dto.response.StudentResponse;
import com.university.universitymanagement.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
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
import java.util.List;

@RestController
@RequestMapping("/api/courses")
@Tag(name = "Courses", description = "Manage courses and view their enrolled students")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping
    @Operation(summary = "List courses",
            description = "Paginated and sortable. Filter by part of the title and/or department id.")
    public PageResponse<CourseResponse> getAll(
            @Parameter(description = "Part of the course title")
            @RequestParam(required = false) String title,
            @Parameter(description = "Only courses of this department")
            @RequestParam(required = false) Long departmentId,
            @ParameterObject @PageableDefault(size = 10, sort = "title") Pageable pageable) {
        return courseService.getAll(title, departmentId, pageable);
    }

    @GetMapping("/popular")
    @Operation(summary = "Courses ordered by number of active enrollments (most popular first)")
    public List<CoursePopularityResponse> getMostPopular(
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int limit) {
        return courseService.getMostPopular(limit);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a course by id")
    public CourseResponse getById(@PathVariable Long id) {
        return courseService.getById(id);
    }

    @GetMapping("/{id}/students")
    @Operation(summary = "List all students actively enrolled in the course")
    public List<StudentResponse> getStudents(@PathVariable Long id) {
        return courseService.getStudents(id);
    }

    @PostMapping
    @Operation(summary = "Create a course")
    public ResponseEntity<CourseResponse> create(@Valid @RequestBody CourseRequest request) {
        CourseResponse created = courseService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a course",
            description = "Capacity cannot be reduced below the current number of active enrollments.")
    public CourseResponse update(@PathVariable Long id, @Valid @RequestBody CourseRequest request) {
        return courseService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a course", description = "Fails with 409 if the course has active enrollments.")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        courseService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
