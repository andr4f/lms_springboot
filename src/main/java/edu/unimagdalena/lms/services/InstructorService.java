package edu.unimagdalena.lms.services;

import edu.unimagdalena.lms.api.dto.InstructorDtos.*;
import java.util.List;
import java.util.UUID;

public interface InstructorService {
    InstructorResponse create(InstructorRequest req);
    InstructorResponse get(UUID id);
    List<InstructorResponse> list();
    InstructorResponse update(UUID id, InstructorUpdateRequest req);
    void delete(UUID id);
}