package edu.unimagdalena.lms.services;

import java.util.List;
import java.util.UUID;

import edu.unimagdalena.lms.api.dto.StudentDtos.*;

public interface StudentService {

    StudentResponse create(StudentRequest req);
    StudentResponse get(UUID id);
    List<StudentResponse> list();
    StudentResponse update(UUID id, StudentUpdateRequest req);
    void delete(UUID id);
    
} 