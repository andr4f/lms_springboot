package edu.unimagdalena.lms.services;

import edu.unimagdalena.lms.api.dto.CourseDtos.*;
import java.util.List;
import java.util.UUID;

public interface CourseService {
    CourseResponse create(CourseRequest req);
    CourseResponse get(UUID id);
    List<CourseResponse> list();
    CourseResponse update(UUID id, CourseUpdateRequest req);
    void delete(UUID id);
}