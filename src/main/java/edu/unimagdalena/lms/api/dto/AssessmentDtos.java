package edu.unimagdalena.lms.api.dto;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

public class AssessmentDtos {

    public record AssessmentRequest(
        UUID studentId,
        UUID courseId,
        Integer score,
        String type
    ) implements Serializable {}

    public record AssessmentUpdateRequest(
        Integer score,
        String type
    ) implements Serializable {}

    public record AssessmentResponse(
        UUID id,
        String type,
        Integer score,
        Instant takenAt,
        UUID studentId,
        UUID courseId
    ) implements Serializable {}
}