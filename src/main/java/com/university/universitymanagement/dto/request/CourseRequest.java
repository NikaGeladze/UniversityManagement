package com.university.universitymanagement.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CourseRequest(

        @Schema(example = "Introduction to Algorithms")
        @NotBlank(message = "Title is required")
        @Size(max = 150, message = "Title must be at most 150 characters")
        String title,

        @Schema(example = "Sorting, searching, graphs and dynamic programming")
        @Size(max = 1000, message = "Description must be at most 1000 characters")
        String description,

        @Schema(example = "6")
        @NotNull(message = "Credits are required")
        @Min(value = 1, message = "Credits must be at least 1")
        @Max(value = 30, message = "Credits must be at most 30")
        Integer credits,

        @Schema(example = "30")
        @NotNull(message = "Capacity is required")
        @Min(value = 1, message = "Capacity must be at least 1")
        @Max(value = 500, message = "Capacity must be at most 500")
        Integer capacity,

        @Schema(example = "1")
        @NotNull(message = "Department id is required")
        Long departmentId
) {
}
