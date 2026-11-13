package com.university.universitymanagement.service;

import com.university.universitymanagement.dto.request.DepartmentRequest;
import com.university.universitymanagement.dto.response.DepartmentResponse;
import com.university.universitymanagement.dto.response.PageResponse;
import com.university.universitymanagement.entity.Department;
import com.university.universitymanagement.exception.BusinessRuleException;
import com.university.universitymanagement.exception.DuplicateResourceException;
import com.university.universitymanagement.exception.ResourceNotFoundException;
import com.university.universitymanagement.repository.CourseRepository;
import com.university.universitymanagement.repository.DepartmentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional(readOnly = true)
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final CourseRepository courseRepository;

    public DepartmentService(DepartmentRepository departmentRepository, CourseRepository courseRepository) {
        this.departmentRepository = departmentRepository;
        this.courseRepository = courseRepository;
    }

    public PageResponse<DepartmentResponse> getAll(String name, Pageable pageable) {
        Page<Department> page = StringUtils.hasText(name)
                ? departmentRepository.findByNameContainingIgnoreCase(name.trim(), pageable)
                : departmentRepository.findAll(pageable);
        return PageResponse.from(page, DepartmentResponse::from);
    }

    public DepartmentResponse getById(Long id) {
        return DepartmentResponse.from(findDepartment(id));
    }

    @Transactional
    public DepartmentResponse create(DepartmentRequest request) {
        String name = request.name().trim();
        if (departmentRepository.existsByNameIgnoreCase(name)) {
            throw new DuplicateResourceException("Department with name '" + name + "' already exists");
        }
        Department department = new Department(name, request.description());
        return DepartmentResponse.from(departmentRepository.save(department));
    }

    @Transactional
    public DepartmentResponse update(Long id, DepartmentRequest request) {
        Department department = findDepartment(id);
        String name = request.name().trim();
        if (departmentRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new DuplicateResourceException("Department with name '" + name + "' already exists");
        }
        department.setName(name);
        department.setDescription(request.description());
        return DepartmentResponse.from(department);
    }

    @Transactional
    public void delete(Long id) {
        Department department = findDepartment(id);
        if (courseRepository.existsByDepartmentId(id)) {
            throw new BusinessRuleException("Cannot delete department '" + department.getName()
                    + "' because it still contains courses");
        }
        departmentRepository.delete(department);
    }

    private Department findDepartment(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", id));
    }
}
