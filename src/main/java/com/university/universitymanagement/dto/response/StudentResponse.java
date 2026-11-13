package com.university.universitymanagement.dto.response;

import com.university.universitymanagement.entity.Student;

import java.time.LocalDateTime;

public record StudentResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        String profileImagePath,
        LocalDateTime createdAt
) {

    public static StudentResponse from(Student student) {
        return new StudentResponse(
                student.getId(),
                student.getFirstName(),
                student.getLastName(),
                student.getEmail(),
                student.getProfileImagePath(),
                student.getCreatedAt()
        );
    }
}
