package com.university.universitymanagement.repository;

import com.university.universitymanagement.entity.EnrollmentStatus;
import com.university.universitymanagement.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    Optional<Student> findByEmail(String email);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    Page<Student> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
            String firstName, String lastName, String email, Pageable pageable);

    @Query("""
            SELECT s FROM Enrollment e
            JOIN e.student s
            WHERE e.course.id = :courseId AND e.status = :status
            ORDER BY s.lastName, s.firstName
            """)
    List<Student> findStudentsByCourseAndStatus(@Param("courseId") Long courseId,
                                                @Param("status") EnrollmentStatus status);
}
