package com.university.universitymanagement.repository;

import com.university.universitymanagement.dto.response.CoursePopularityResponse;
import com.university.universitymanagement.entity.Course;
import com.university.universitymanagement.entity.EnrollmentStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    @Override
    @EntityGraph(attributePaths = "department")
    Page<Course> findAll(Pageable pageable);

    @EntityGraph(attributePaths = "department")
    Page<Course> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    @EntityGraph(attributePaths = "department")
    Page<Course> findByDepartmentId(Long departmentId, Pageable pageable);

    @EntityGraph(attributePaths = "department")
    Page<Course> findByDepartmentIdAndTitleContainingIgnoreCase(Long departmentId, String title, Pageable pageable);

    boolean existsByDepartmentId(Long departmentId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Course c WHERE c.id = :id")
    Optional<Course> findByIdForUpdate(@Param("id") Long id);

    @Query("""
            SELECT new com.university.universitymanagement.dto.response.CoursePopularityResponse(
                c.id, c.title, d.name, c.capacity, COUNT(e.id))
            FROM Course c
            JOIN c.department d
            LEFT JOIN Enrollment e ON e.course = c AND e.status = :status
            GROUP BY c.id, c.title, d.name, c.capacity
            ORDER BY COUNT(e.id) DESC, c.title ASC
            """)
    List<CoursePopularityResponse> findCoursesOrderedByPopularity(@Param("status") EnrollmentStatus status,
                                                                  Pageable pageable);

    @Query("""
            SELECT c FROM Enrollment e
            JOIN e.course c
            JOIN FETCH c.department
            WHERE e.student.id = :studentId AND e.status = :status
            ORDER BY c.title
            """)
    List<Course> findCoursesByStudentAndStatus(@Param("studentId") Long studentId,
                                               @Param("status") EnrollmentStatus status);
}
