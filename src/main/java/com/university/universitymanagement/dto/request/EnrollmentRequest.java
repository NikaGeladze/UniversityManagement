package com.university.universitymanagement.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record EnrollmentRequest(

        @Schema(example = "1")
        @NotNull(message = "Student id is required")
        Long studentId,

        @Schema(example = "1")
        @NotNull(message = "Course id is required")
        Long courseId
) {
}
