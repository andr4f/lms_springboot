package edu.unimagdalena.lms.services;

import edu.unimagdalena.lms.api.dto.LessonDtos.*;
import edu.unimagdalena.lms.exception.NotFoundException;
import edu.unimagdalena.lms.repositories.CourseRepository;
import edu.unimagdalena.lms.repositories.LessonRepository;
import edu.unimagdalena.lms.services.mapper.LessonMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class LessonServiceImpl implements LessonService {

    private final LessonRepository lessonRepository;
    private final CourseRepository courseRepository;

    @Override
    @Transactional
    public LessonResponse create(LessonRequest req) {
        var course = courseRepository.findById(req.courseId())
                .orElseThrow(() -> new NotFoundException(
                    "Course %s not found".formatted(req.courseId())));
        var entity = LessonMapper.toEntity(req, course);
        var saved = lessonRepository.save(entity);
        return LessonMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public LessonResponse get(UUID id) {
        return lessonRepository.findById(id)
                .map(LessonMapper::toResponse)
                .orElseThrow(() -> new NotFoundException(
                    "Lesson %s not found".formatted(id)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<LessonResponse> list() {
        return lessonRepository.findAll()
                .stream()
                .map(LessonMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LessonResponse> listByCourse(UUID courseId) {
        return lessonRepository.findByCourseId(courseId)
                .stream()
                .map(LessonMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
   public LessonResponse update(UUID id, LessonUpdateRequest req) {
    var entity = lessonRepository.findById(id)
            .orElseThrow(() -> new NotFoundException(
                "Lesson %s not found".formatted(id)));

    // si viene nuevo courseId, resolvemos el curso
    if (req.courseId() != null) {
        var course = courseRepository.findById(req.courseId())
                .orElseThrow(() -> new NotFoundException(
                    "Course %s not found".formatted(req.courseId())));
        entity.setCourse(course);
    }

    LessonMapper.patch(entity, req); // solo parchea title y orderIndex
    var saved = lessonRepository.save(entity);
    return LessonMapper.toResponse(saved);
}
    // You may want to update other fields here as well

    @Override
    public void delete(UUID id) {
        if (!lessonRepository.existsById(id)) {
            throw new NotFoundException("Lesson %s not found".formatted(id));
        }
        lessonRepository.deleteById(id);
    }
}