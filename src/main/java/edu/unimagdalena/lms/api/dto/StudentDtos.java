package edu.unimagdalena.lms.api.dto;

import java.io.Serializable;
import java.util.UUID;
import java.time.Instant;

public class StudentDtos {
    

    public record StudentRequest(
            String email,
            String fullName
    ) implements Serializable {}

    public record StudentUpdateRequest(
            String email,
            String fullName
    ) implements Serializable {}

    public record StudentResponse(
            UUID id,
            String email,
            String fullName,
            Instant createdAt
    ) implements Serializable {}
}
