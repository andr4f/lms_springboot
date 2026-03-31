package edu.unimagdalena.lms.services;

import edu.unimagdalena.lms.api.dto.AssessmentDtos.*;
import java.util.*;
import java.util.UUID;

public interface AssessmentService {
    AssessmentResponse create(AssessmentRequest req);

    AssessmentResponse get(UUID id);

    List<AssessmentResponse> list();

    List<AssessmentResponse> listByStudent(UUID studentId);

    List<AssessmentResponse> listByCourse(UUID courseId);

    AssessmentResponse update(UUID id, AssessmentUpdateRequest req);

    void delete(UUID id);
}
