package edu.unimagdalena.lms.api.dto;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

public class CourseDtos {

    public record CourseRequest(
        String title,
        UUID instructorId
    ) implements Serializable {}

    public record CourseUpdateRequest(
        String title,
        String status,
        Boolean active
    ) implements Serializable {}

    public record CourseResponse(
        UUID id,
        String title,
        String status,
        Boolean active,
        Instant createdAt,
        UUID instructorId
    ) implements Serializable {}
}