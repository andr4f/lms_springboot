package edu.unimagdalena.lms.services;

import edu.unimagdalena.lms.api.dto.EnrollmentDtos.*;
import java.util.List;
import java.util.UUID;

public interface EnrollmentService {
    EnrollmentResponse create(EnrollmentRequest req);

    EnrollmentResponse get(UUID id);

    List<EnrollmentResponse> list();

    List<EnrollmentResponse> listByStudent(UUID studentId);

    List<EnrollmentResponse> listByCourse(UUID courseId);

    EnrollmentResponse update(UUID id, EnrollmentUpdateRequest req);

    void delete(UUID id);
}
