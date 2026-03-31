package edu.unimagdalena.lms.api.dto;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

public class InstructorDtos {

    public record InstructorProfileDto(
        String phone,
        String bio
    ) implements Serializable {}

    public record InstructorRequest(
            String email,
            String fullName,
            InstructorProfileDto profile
    ) implements Serializable {}

    public record InstructorUpdateRequest(
            String email,
            String fullName,
            InstructorProfileDto profile,
            Instant updatedAt
    ) implements Serializable {}

    public record InstructorResponse(
            UUID id,
            String email,
            String fullName,
            InstructorProfileDto profile,
            Instant createdAt
    ) implements Serializable {}

}
