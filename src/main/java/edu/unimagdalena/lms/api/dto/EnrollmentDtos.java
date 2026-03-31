package edu.unimagdalena.lms.api.dto;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

public class EnrollmentDtos {

    public record EnrollmentRequest(
        UUID studentId,
        UUID courseId
    ) implements Serializable {}

    public record EnrollmentUpdateRequest(
        String status
    ) implements Serializable {}

    public record EnrollmentResponse(
        UUID id,
        String status,
        Instant enrolledAt,
        UUID studentId,
        UUID courseId
    ) implements Serializable {}
}