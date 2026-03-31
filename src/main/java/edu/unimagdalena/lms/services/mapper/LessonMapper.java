package edu.unimagdalena.lms.services.mapper;

import edu.unimagdalena.lms.api.dto.LessonDtos.*;
import edu.unimagdalena.lms.entities.Course;
import edu.unimagdalena.lms.entities.Lesson;
public class LessonMapper {
    
    public static Lesson toEntity(LessonRequest req, Course course) {
        return Lesson.builder()
                .title(req.title())
                .orderIndex(req.orderIndex())
                .course(course)
                .build();
    }

    public static LessonResponse toResponse(Lesson lesson) {
        return new LessonResponse(
                lesson.getId(),
                lesson.getTitle(),
                lesson.getOrderIndex(),
                lesson.getCourse().getId()   // ← extraes solo el UUID para el DTO
        );
    }

    public static void patch(Lesson entity, LessonUpdateRequest req) {
        if (req.title() != null) entity.setTitle(req.title());
        if (req.orderIndex() != null) entity.setOrderIndex(req.orderIndex());
    }
}
