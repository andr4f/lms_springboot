package edu.unimagdalena.lms.services.mapper;

import edu.unimagdalena.lms.entities.Student;

import java.time.Instant;

// Import StudentRequest if it exists in your project, otherwise define it
import edu.unimagdalena.lms.api.dto.StudentDtos.*;

public class StudentMapper {
    
    public static Student toEntity(StudentRequest req) {
        return Student.builder()
                .email(req.email())
                .fullName(req.fullName())
                .build();
    }

    public static void patch(Student entity, StudentUpdateRequest req) {
        if (req.email() != null) entity.setEmail(req.email());
        if (req.fullName() != null) entity.setFullName(req.fullName());
        entity.setUpdatedAt(Instant.now()); // ← siempre actualiza la fecha
}

    public static StudentResponse toResponse(Student student) {
        return new StudentResponse(
                student.getId(),
                student.getEmail(),
                student.getFullName(),
                student.getCreatedAt()
        );
    }
}
