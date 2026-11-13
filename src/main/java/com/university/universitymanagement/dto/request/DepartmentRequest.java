package com.university.universitymanagement.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DepartmentRequest(

        @Schema(example = "Computer Science")
        @NotBlank(message = "Department name is required")
        @Size(min = 2, max = 100, message = "Department name must be between 2 and 100 characters")
        String name,

        @Schema(example = "Software engineering, algorithms and data science")
        @Size(max = 500, message = "Description must be at most 500 characters")
        String description
) {
}
