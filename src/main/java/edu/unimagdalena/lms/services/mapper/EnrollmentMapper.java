package edu.unimagdalena.lms.services.mapper;

import edu.unimagdalena.lms.api.dto.EnrollmentDtos.*;
import edu.unimagdalena.lms.entities.Course;
import edu.unimagdalena.lms.entities.Enrollment;
import edu.unimagdalena.lms.entities.Student;
import java.time.Instant;

public class EnrollmentMapper {

    // Recibe Student y Course ya resueltos por el Service
    public static Enrollment toEntity(EnrollmentRequest req, Student student, Course course) {
        return Enrollment.builder()
                .student(student)
                .course(course)
                .status("ACTIVE")        // ← el mapper asigna el estado inicial
                .enrolledAt(Instant.now()) // ← el mapper asigna la fecha
                .build();
    }

    public static EnrollmentResponse toResponse(Enrollment enrollment) {
        return new EnrollmentResponse(
                enrollment.getId(),
                enrollment.getStatus(),
                enrollment.getEnrolledAt(),
                enrollment.getStudent().getId(),  // ← extrae solo el UUID
                enrollment.getCourse().getId()    // ← extrae solo el UUID
        );
    }

    public static void patch(Enrollment entity, EnrollmentUpdateRequest req) {
        if (req.status() != null) entity.setStatus(req.status());
    }
}