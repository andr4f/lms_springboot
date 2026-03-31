package edu.unimagdalena.lms.services;

import edu.unimagdalena.lms.api.dto.CourseDtos.*;
import edu.unimagdalena.lms.exception.NotFoundException;
import edu.unimagdalena.lms.repositories.CourseRepository;
import edu.unimagdalena.lms.repositories.InstructorRepository;
import edu.unimagdalena.lms.services.mapper.CourseMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final InstructorRepository instructorRepository; // ← necesario para resolver el instructorId

    @Override
    @Transactional
    public CourseResponse create(CourseRequest req) {
        var instructor = instructorRepository.findById(req.instructorId())
                .orElseThrow(() -> new NotFoundException(
                    "Instructor %s not found".formatted(req.instructorId())));
        var entity = CourseMapper.toEntity(req, instructor);
        var saved = courseRepository.save(entity);
        return CourseMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public CourseResponse get(UUID id) {
        return courseRepository.findById(id)
                .map(CourseMapper::toResponse)
                .orElseThrow(() -> new NotFoundException(
                    "Course %s not found".formatted(id)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseResponse> list() {
        return courseRepository.findAll()
                .stream()
                .map(CourseMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public CourseResponse update(UUID id, CourseUpdateRequest req) {
        var entity = courseRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                    "Course %s not found".formatted(id)));
        CourseMapper.patch(entity, req);
        var saved = courseRepository.save(entity);
        return CourseMapper.toResponse(saved);
    }

    @Override
    public void delete(UUID id) {
        if (!courseRepository.existsById(id)) {
            throw new NotFoundException("Course %s not found".formatted(id));
        }
        courseRepository.deleteById(id);
    }
}