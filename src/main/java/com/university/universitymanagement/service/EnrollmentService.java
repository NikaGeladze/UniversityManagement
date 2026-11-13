package com.university.universitymanagement.service;

import com.university.universitymanagement.dto.request.EnrollmentRequest;
import com.university.universitymanagement.dto.response.EnrollmentResponse;
import com.university.universitymanagement.dto.response.PageResponse;
import com.university.universitymanagement.entity.Course;
import com.university.universitymanagement.entity.Enrollment;
import com.university.universitymanagement.entity.EnrollmentStatus;
import com.university.universitymanagement.entity.Student;
import com.university.universitymanagement.exception.BusinessRuleException;
import com.university.universitymanagement.exception.DuplicateResourceException;
import com.university.universitymanagement.exception.ResourceNotFoundException;
import com.university.universitymanagement.repository.CourseRepository;
import com.university.universitymanagement.repository.EnrollmentRepository;
import com.university.universitymanagement.repository.StudentRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;

    public EnrollmentService(EnrollmentRepository enrollmentRepository,
                             StudentRepository studentRepository,
                             CourseRepository courseRepository) {
        this.enrollmentRepository = enrollmentRepository;
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
    }

    public PageResponse<EnrollmentResponse> getAll(Long studentId, Long courseId, EnrollmentStatus status,
                                                   Pageable pageable) {
        return PageResponse.from(enrollmentRepository.search(studentId, courseId, status, pageable),
                EnrollmentResponse::from);
    }

    public EnrollmentResponse getById(Long id) {
        return EnrollmentResponse.from(findEnrollment(id));
    }

    /**
     * The course row is locked for the duration of the transaction so that concurrent
     * enrollments cannot exceed its capacity.
     */
    @Transactional
    public EnrollmentResponse enroll(EnrollmentRequest request) {
        Student student = studentRepository.findById(request.studentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student", request.studentId()));
        Course course = courseRepository.findByIdForUpdate(request.courseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course", request.courseId()));

        Optional<Enrollment> existing = enrollmentRepository.findByStudentIdAndCourseId(student.getId(), course.getId());
        if (existing.isPresent() && existing.get().isActive()) {
            throw new DuplicateResourceException("Student " + student.getId()
                    + " is already enrolled in course " + course.getId());
        }

        long activeEnrollments = enrollmentRepository.countByCourseIdAndStatus(course.getId(), EnrollmentStatus.ACTIVE);
        if (activeEnrollments >= course.getCapacity()) {
            throw new BusinessRuleException("Course '" + course.getTitle() + "' has reached its capacity of "
                    + course.getCapacity());
        }

        Enrollment enrollment;
        if (existing.isPresent()) {
            enrollment = existing.get();
            enrollment.activate();
        } else {
            enrollment = enrollmentRepository.save(new Enrollment(student, course));
        }
        return EnrollmentResponse.from(enrollment);
    }

    @Transactional
    public EnrollmentResponse cancel(Long id) {
        Enrollment enrollment = findEnrollment(id);
        if (!enrollment.isActive()) {
            throw new BusinessRuleException("Enrollment with id " + id + " is already cancelled");
        }
        enrollment.cancel();
        return EnrollmentResponse.from(enrollment);
    }

    private Enrollment findEnrollment(Long id) {
        return enrollmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment", id));
    }
}
