package edu.unimagdalena.lms.services;

import edu.unimagdalena.lms.api.dto.EnrollmentDtos.*;
import edu.unimagdalena.lms.exception.NotFoundException;
import edu.unimagdalena.lms.repositories.CourseRepository;
import edu.unimagdalena.lms.repositories.EnrollmentRepository;
import edu.unimagdalena.lms.repositories.StudentRepository;
import edu.unimagdalena.lms.services.mapper.EnrollmentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class EnrollmentServiceImpl implements EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;

    @Override
    @Transactional
    public EnrollmentResponse create(EnrollmentRequest req) {
        var student = studentRepository.findById(req.studentId())
                .orElseThrow(() -> new NotFoundException(
                        "Student %s not found".formatted(req.studentId())));
        var course = courseRepository.findById(req.courseId())
                .orElseThrow(() -> new NotFoundException(
                        "Course %s not found".formatted(req.courseId())));
        var entity = EnrollmentMapper.toEntity(req, student, course);
        var saved = enrollmentRepository.save(entity);
        return EnrollmentMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public EnrollmentResponse get(UUID id) {
        return enrollmentRepository.findById(id)
                .map(EnrollmentMapper::toResponse)
                .orElseThrow(() -> new NotFoundException(
                        "Enrollment %s not found".formatted(id)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EnrollmentResponse> list() {
        return enrollmentRepository.findAll()
                .stream()
                .map(EnrollmentMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EnrollmentResponse> listByStudent(UUID studentId) {
        return enrollmentRepository.findByStudentId(studentId)
                .stream()
                .map(EnrollmentMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EnrollmentResponse> listByCourse(UUID courseId) {
        return enrollmentRepository.findByCourseId(courseId)
                .stream()
                .map(EnrollmentMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public EnrollmentResponse update(UUID id, EnrollmentUpdateRequest req) {
        var entity = enrollmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        "Enrollment %s not found".formatted(id)));
        EnrollmentMapper.patch(entity, req);
        var saved = enrollmentRepository.save(entity);
        return EnrollmentMapper.toResponse(saved);
    }

    @Override
    public void delete(UUID id) {
        if (!enrollmentRepository.existsById(id)) {
            throw new NotFoundException("Enrollment %s not found".formatted(id));
        }
        enrollmentRepository.deleteById(id);
    }
}
