package com.university.universitymanagement.dto.response;

import com.university.universitymanagement.entity.Enrollment;
import com.university.universitymanagement.entity.EnrollmentStatus;

import java.time.LocalDateTime;

public record EnrollmentResponse(
        Long id,
        Long studentId,
        String studentName,
        Long courseId,
        String courseTitle,
        LocalDateTime enrollmentDate,
        EnrollmentStatus status
) {

    public static EnrollmentResponse from(Enrollment enrollment) {
        return new EnrollmentResponse(
                enrollment.getId(),
                enrollment.getStudent().getId(),
                enrollment.getStudent().getFirstName() + " " + enrollment.getStudent().getLastName(),
                enrollment.getCourse().getId(),
                enrollment.getCourse().getTitle(),
                enrollment.getEnrollmentDate(),
                enrollment.getStatus()
        );
    }
}
