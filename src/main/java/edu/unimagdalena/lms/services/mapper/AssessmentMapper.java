package edu.unimagdalena.lms.services.mapper;

import edu.unimagdalena.lms.api.dto.AssessmentDtos.*;
import edu.unimagdalena.lms.entities.Assessment;
import edu.unimagdalena.lms.entities.Course;
import edu.unimagdalena.lms.entities.Student;
import java.time.Instant;

public class AssessmentMapper {

    public static Assessment toEntity(AssessmentRequest req, Student student, Course course) {
        return Assessment.builder()
                .student(student)
                .course(course)
                .score(req.score())
                .type(req.type())
                .takenAt(Instant.now())   // ← el mapper asigna la fecha
                .build();
    }

    public static AssessmentResponse toResponse(Assessment assessment) {
        return new AssessmentResponse(
                assessment.getId(),
                assessment.getType(),
                assessment.getScore(),
                assessment.getTakenAt(),
                assessment.getStudent().getId(),  // ← extrae solo el UUID
                assessment.getCourse().getId()    // ← extrae solo el UUID
        );
    }

    public static void patch(Assessment entity, AssessmentUpdateRequest req) {
        if (req.score() != null) entity.setScore(req.score());
        if (req.type() != null) entity.setType(req.type());
    }
}