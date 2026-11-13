package com.university.universitymanagement.service;

import com.university.universitymanagement.dto.request.CourseRequest;
import com.university.universitymanagement.dto.response.CoursePopularityResponse;
import com.university.universitymanagement.dto.response.CourseResponse;
import com.university.universitymanagement.dto.response.PageResponse;
import com.university.universitymanagement.dto.response.StudentResponse;
import com.university.universitymanagement.entity.Course;
import com.university.universitymanagement.entity.Department;
import com.university.universitymanagement.entity.EnrollmentStatus;
import com.university.universitymanagement.exception.BusinessRuleException;
import com.university.universitymanagement.exception.ResourceNotFoundException;
import com.university.universitymanagement.repository.CourseRepository;
import com.university.universitymanagement.repository.DepartmentRepository;
import com.university.universitymanagement.repository.EnrollmentRepository;
import com.university.universitymanagement.repository.StudentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class CourseService {

    private final CourseRepository courseRepository;
    private final DepartmentRepository departmentRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;

    public CourseService(CourseRepository courseRepository,
                         DepartmentRepository departmentRepository,
                         EnrollmentRepository enrollmentRepository,
                         StudentRepository studentRepository) {
        this.courseRepository = courseRepository;
        this.departmentRepository = departmentRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.studentRepository = studentRepository;
    }

    public PageResponse<CourseResponse> getAll(String title, Long departmentId, Pageable pageable) {
        boolean hasTitle = StringUtils.hasText(title);
        Page<Course> page;
        if (hasTitle && departmentId != null) {
            page = courseRepository.findByDepartmentIdAndTitleContainingIgnoreCase(departmentId, title.trim(), pageable);
        } else if (departmentId != null) {
            page = courseRepository.findByDepartmentId(departmentId, pageable);
        } else if (hasTitle) {
            page = courseRepository.findByTitleContainingIgnoreCase(title.trim(), pageable);
        } else {
            page = courseRepository.findAll(pageable);
        }
        return PageResponse.from(page, CourseResponse::from);
    }

    public CourseResponse getById(Long id) {
        return CourseResponse.from(findCourse(id));
    }

    public List<StudentResponse> getStudents(Long courseId) {
        if (!courseRepository.existsById(courseId)) {
            throw new ResourceNotFoundException("Course", courseId);
        }
        return studentRepository.findStudentsByCourseAndStatus(courseId, EnrollmentStatus.ACTIVE).stream()
                .map(StudentResponse::from)
                .toList();
    }

    public List<CoursePopularityResponse> getMostPopular(int limit) {
        return courseRepository.findCoursesOrderedByPopularity(EnrollmentStatus.ACTIVE, PageRequest.of(0, limit));
    }

    @Transactional
    public CourseResponse create(CourseRequest request) {
        Department department = findDepartment(request.departmentId());
        Course course = new Course(
                request.title().trim(),
                request.description(),
                request.credits(),
                request.capacity(),
                department
        );
        return CourseResponse.from(courseRepository.save(course));
    }

    @Transactional
    public CourseResponse update(Long id, CourseRequest request) {
        Course course = courseRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course", id));

        long activeEnrollments = enrollmentRepository.countByCourseIdAndStatus(id, EnrollmentStatus.ACTIVE);
        if (request.capacity() < activeEnrollments) {
            throw new BusinessRuleException("Capacity cannot be lower than the current number of active enrollments ("
                    + activeEnrollments + ")");
        }

        if (!course.getDepartment().getId().equals(request.departmentId())) {
            course.setDepartment(findDepartment(request.departmentId()));
        }
        course.setTitle(request.title().trim());
        course.setDescription(request.description());
        course.setCredits(request.credits());
        course.setCapacity(request.capacity());
        return CourseResponse.from(course);
    }

    @Transactional
    public void delete(Long id) {
        Course course = findCourse(id);
        if (enrollmentRepository.existsByCourseIdAndStatus(id, EnrollmentStatus.ACTIVE)) {
            throw new BusinessRuleException("Cannot delete course with id " + id + " because it has active enrollments");
        }
        enrollmentRepository.deleteAllByCourseId(id);
        courseRepository.delete(course);
    }

    private Course findCourse(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course", id));
    }

    private Department findDepartment(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", id));
    }
}
