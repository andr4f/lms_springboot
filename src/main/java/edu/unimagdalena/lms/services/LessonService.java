package edu.unimagdalena.lms.services;

import edu.unimagdalena.lms.api.dto.LessonDtos.*;
import java.util.List;
import java.util.UUID;

public interface LessonService {
    LessonResponse create(LessonRequest req);
    LessonResponse get(UUID id);
    List<LessonResponse> list();
    List<LessonResponse> listByCourse(UUID courseId);
    LessonResponse update(UUID id, LessonUpdateRequest req);
    void delete(UUID id);
}