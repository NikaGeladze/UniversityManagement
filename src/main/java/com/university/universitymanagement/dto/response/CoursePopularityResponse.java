package com.university.universitymanagement.dto.response;

public record CoursePopularityResponse(
        Long courseId,
        String title,
        String departmentName,
        Integer capacity,
        Long activeEnrollments
) {
}
