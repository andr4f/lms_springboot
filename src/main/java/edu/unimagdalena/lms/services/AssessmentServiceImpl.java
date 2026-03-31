package edu.unimagdalena.lms.services;

import edu.unimagdalena.lms.api.dto.AssessmentDtos.*;
import edu.unimagdalena.lms.exception.NotFoundException;
import edu.unimagdalena.lms.repositories.AssessmentRepository;
import edu.unimagdalena.lms.repositories.CourseRepository;
import edu.unimagdalena.lms.repositories.StudentRepository;
import edu.unimagdalena.lms.services.mapper.AssessmentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AssessmentServiceImpl implements AssessmentService {

    private final AssessmentRepository assessmentRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;

    @Override
    @Transactional
    public AssessmentResponse create(AssessmentRequest req) {
        var student = studentRepository.findById(req.studentId())
                .orElseThrow(() -> new NotFoundException(
                        "Student %s not found".formatted(req.studentId())));
        var course = courseRepository.findById(req.courseId())
                .orElseThrow(() -> new NotFoundException(
                        "Course %s not found".formatted(req.courseId())));
        var entity = AssessmentMapper.toEntity(req, student, course);
        var saved = assessmentRepository.save(entity);
        return AssessmentMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public AssessmentResponse get(UUID id) {
        return assessmentRepository.findById(id)
                .map(AssessmentMapper::toResponse)
                .orElseThrow(() -> new NotFoundException(
                        "Assessment %s not found".formatted(id)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssessmentResponse> list() {
        return assessmentRepository.findAll()
                .stream()
                .map(AssessmentMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssessmentResponse> listByStudent(UUID studentId) {
        return assessmentRepository.findByStudentId(studentId)
                .stream()
                .map(AssessmentMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssessmentResponse> listByCourse(UUID courseId) {
        return assessmentRepository.findByCourseId(courseId)
                .stream()
                .map(AssessmentMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public AssessmentResponse update(UUID id, AssessmentUpdateRequest req) {
        var entity = assessmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        "Assessment %s not found".formatted(id)));
        AssessmentMapper.patch(entity, req);
        var saved = assessmentRepository.save(entity);
        return AssessmentMapper.toResponse(saved);
    }

    @Override
    public void delete(UUID id) {
        if (!assessmentRepository.existsById(id)) {
            throw new NotFoundException("Assessment %s not found".formatted(id));
        }
        assessmentRepository.deleteById(id);
    }
}
