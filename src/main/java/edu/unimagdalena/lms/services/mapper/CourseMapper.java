package edu.unimagdalena.lms.services.mapper;

import edu.unimagdalena.lms.api.dto.CourseDtos.*;
import edu.unimagdalena.lms.entities.Course;
import edu.unimagdalena.lms.entities.Instructor;
import java.time.Instant;

public class CourseMapper {

    public static Course toEntity(CourseRequest req, Instructor instructor) {
        return Course.builder()
                .title(req.title())
                .instructor(instructor)
                .status("DRAFT")         // ← estado inicial por defecto
                .active(false)           // ← inactivo por defecto al crear
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    public static CourseResponse toResponse(Course course) {
        return new CourseResponse(
                course.getId(),
                course.getTitle(),
                course.getStatus(),
                course.getActive(),
                course.getCreatedAt(),
                course.getInstructor().getId()  // ← extrae solo el UUID
        );
    }

    public static void patch(Course entity, CourseUpdateRequest req) {
        if (req.title() != null) entity.setTitle(req.title());
        if (req.status() != null) entity.setStatus(req.status());
        if (req.active() != null) entity.setActive(req.active());
        entity.setUpdatedAt(Instant.now());
    }
}