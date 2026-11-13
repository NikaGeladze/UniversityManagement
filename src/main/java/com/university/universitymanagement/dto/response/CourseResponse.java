package com.university.universitymanagement.dto.response;

import com.university.universitymanagement.entity.Course;

import java.time.LocalDateTime;

public record CourseResponse(
        Long id,
        String title,
        String description,
        Integer credits,
        Integer capacity,
        Long departmentId,
        String departmentName,
        LocalDateTime createdAt
) {

    public static CourseResponse from(Course course) {
        return new CourseResponse(
                course.getId(),
                course.getTitle(),
                course.getDescription(),
                course.getCredits(),
                course.getCapacity(),
                course.getDepartment().getId(),
                course.getDepartment().getName(),
                course.getCreatedAt()
        );
    }
}
