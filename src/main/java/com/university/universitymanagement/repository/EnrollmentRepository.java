package com.university.universitymanagement.repository;

import com.university.universitymanagement.entity.Enrollment;
import com.university.universitymanagement.entity.EnrollmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    Optional<Enrollment> findByStudentIdAndCourseId(Long studentId, Long courseId);

    long countByCourseIdAndStatus(Long courseId, EnrollmentStatus status);

    boolean existsByStudentIdAndStatus(Long studentId, EnrollmentStatus status);

    boolean existsByCourseIdAndStatus(Long courseId, EnrollmentStatus status);

    @Override
    @EntityGraph(attributePaths = {"student", "course"})
    Optional<Enrollment> findById(Long id);

    @EntityGraph(attributePaths = {"student", "course"})
    @Query("""
            SELECT e FROM Enrollment e
            WHERE (:studentId IS NULL OR e.student.id = :studentId)
              AND (:courseId IS NULL OR e.course.id = :courseId)
              AND (:status IS NULL OR e.status = :status)
            """)
    Page<Enrollment> search(@Param("studentId") Long studentId,
                            @Param("courseId") Long courseId,
                            @Param("status") EnrollmentStatus status,
                            Pageable pageable);

    @Modifying
    @Query("DELETE FROM Enrollment e WHERE e.student.id = :studentId")
    void deleteAllByStudentId(@Param("studentId") Long studentId);

    @Modifying
    @Query("DELETE FROM Enrollment e WHERE e.course.id = :courseId")
    void deleteAllByCourseId(@Param("courseId") Long courseId);
}
