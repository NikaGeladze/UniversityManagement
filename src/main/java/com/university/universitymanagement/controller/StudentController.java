package com.university.universitymanagement.controller;

import com.university.universitymanagement.dto.request.StudentRequest;
import com.university.universitymanagement.dto.response.CourseResponse;
import com.university.universitymanagement.dto.response.PageResponse;
import com.university.universitymanagement.dto.response.StudentResponse;
import com.university.universitymanagement.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/students")
@Tag(name = "Students", description = "Manage students and their profile images")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping
    @Operation(summary = "List students",
            description = "Paginated and sortable. Use 'search' to filter by first name, last name or email.")
    public PageResponse<StudentResponse> getAll(
            @Parameter(description = "Search term matched against first name, last name and email")
            @RequestParam(required = false) String search,
            @ParameterObject @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        return studentService.getAll(search, pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a student by id")
    public StudentResponse getById(@PathVariable Long id) {
        return studentService.getById(id);
    }

    @GetMapping("/by-email")
    @Operation(summary = "Find a student by email")
    public StudentResponse getByEmail(@RequestParam @Email String email) {
        return studentService.getByEmail(email);
    }

    @GetMapping("/{id}/courses")
    @Operation(summary = "List all courses the student is actively enrolled in")
    public List<CourseResponse> getCourses(@PathVariable Long id) {
        return studentService.getCourses(id);
    }

    @PostMapping
    @Operation(summary = "Create a student")
    public ResponseEntity<StudentResponse> create(@Valid @RequestBody StudentRequest request) {
        StudentResponse created = studentService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a student")
    public StudentResponse update(@PathVariable Long id, @Valid @RequestBody StudentRequest request) {
        return studentService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a student", description = "Fails with 409 if the student has active enrollments.")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        studentService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/{id}/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload a student profile image",
            description = "Accepts JPEG, PNG or WEBP images up to 2 MB. Replaces any existing image.")
    public StudentResponse uploadProfileImage(
            @PathVariable Long id,
            @Parameter(description = "Image file (jpg, png, webp)") @RequestParam("file") MultipartFile file) {
        return studentService.uploadProfileImage(id, file);
    }
}
