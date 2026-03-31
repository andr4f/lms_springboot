package edu.unimagdalena.lms.api.dto;
import java.io.Serializable;

import java.util.UUID;

public class LessonDtos {
    
    public record LessonRequest(
            String title, 
            int orderIndex, 
            UUID courseId 
    ) implements Serializable {}

    public record LessonUpdateRequest(
            String title,
            Integer orderIndex,
            UUID courseId
    ) implements Serializable {}

    public record LessonResponse(
            UUID id,
            String title,
            int orderIndex,
            UUID courseId
    ) implements Serializable {}
}
