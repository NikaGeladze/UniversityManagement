package com.university.universitymanagement.service;

import com.university.universitymanagement.dto.request.StudentRequest;
import com.university.universitymanagement.dto.response.CourseResponse;
import com.university.universitymanagement.dto.response.PageResponse;
import com.university.universitymanagement.dto.response.StudentResponse;
import com.university.universitymanagement.entity.EnrollmentStatus;
import com.university.universitymanagement.entity.Student;
import com.university.universitymanagement.exception.BusinessRuleException;
import com.university.universitymanagement.exception.DuplicateResourceException;
import com.university.universitymanagement.exception.ResourceNotFoundException;
import com.university.universitymanagement.repository.CourseRepository;
import com.university.universitymanagement.repository.EnrollmentRepository;
import com.university.universitymanagement.repository.StudentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class StudentService {

    private static final String PROFILE_IMAGE_DIR = "students";

    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final FileStorageService fileStorageService;

    public StudentService(StudentRepository studentRepository,
                          CourseRepository courseRepository,
                          EnrollmentRepository enrollmentRepository,
                          FileStorageService fileStorageService) {
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.fileStorageService = fileStorageService;
    }

    public PageResponse<StudentResponse> getAll(String search, Pageable pageable) {
        Page<Student> page;
        if (StringUtils.hasText(search)) {
            String term = search.trim();
            page = studentRepository
                    .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                            term, term, term, pageable);
        } else {
            page = studentRepository.findAll(pageable);
        }
        return PageResponse.from(page, StudentResponse::from);
    }

    public StudentResponse getById(Long id) {
        return StudentResponse.from(findStudent(id));
    }

    public StudentResponse getByEmail(String email) {
        return studentRepository.findByEmail(email.trim().toLowerCase())
                .map(StudentResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Student with email '" + email + "' not found"));
    }

    public List<CourseResponse> getCourses(Long studentId) {
        ensureStudentExists(studentId);
        return courseRepository.findCoursesByStudentAndStatus(studentId, EnrollmentStatus.ACTIVE).stream()
                .map(CourseResponse::from)
                .toList();
    }

    @Transactional
    public StudentResponse create(StudentRequest request) {
        String email = normalizeEmail(request.email());
        if (studentRepository.existsByEmailIgnoreCase(email)) {
            throw new DuplicateResourceException("Student with email '" + email + "' already exists");
        }
        Student student = new Student(request.firstName().trim(), request.lastName().trim(), email);
        return StudentResponse.from(studentRepository.save(student));
    }

    @Transactional
    public StudentResponse update(Long id, StudentRequest request) {
        Student student = findStudent(id);
        String email = normalizeEmail(request.email());
        if (studentRepository.existsByEmailIgnoreCaseAndIdNot(email, id)) {
            throw new DuplicateResourceException("Student with email '" + email + "' already exists");
        }
        student.setFirstName(request.firstName().trim());
        student.setLastName(request.lastName().trim());
        student.setEmail(email);
        return StudentResponse.from(student);
    }

    @Transactional
    public void delete(Long id) {
        Student student = findStudent(id);
        if (enrollmentRepository.existsByStudentIdAndStatus(id, EnrollmentStatus.ACTIVE)) {
            throw new BusinessRuleException("Cannot delete student with id " + id + " because they have active enrollments");
        }
        enrollmentRepository.deleteAllByStudentId(id);
        studentRepository.delete(student);
        fileStorageService.delete(student.getProfileImagePath());
    }

    @Transactional
    public StudentResponse uploadProfileImage(Long id, MultipartFile file) {
        Student student = findStudent(id);
        String previousPath = student.getProfileImagePath();
        String newPath = fileStorageService.store(file, PROFILE_IMAGE_DIR);
        student.setProfileImagePath(newPath);
        studentRepository.flush();
        fileStorageService.delete(previousPath);
        return StudentResponse.from(student);
    }

    private Student findStudent(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student", id));
    }

    private void ensureStudentExists(Long id) {
        if (!studentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Student", id);
        }
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }
}
