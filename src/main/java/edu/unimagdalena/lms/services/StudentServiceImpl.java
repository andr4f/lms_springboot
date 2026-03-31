package edu.unimagdalena.lms.services;

import edu.unimagdalena.lms.api.dto.StudentDtos.*;
import edu.unimagdalena.lms.exception.NotFoundException;
import edu.unimagdalena.lms.repositories.StudentRepository;
import edu.unimagdalena.lms.services.mapper.StudentMapper;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional

public class StudentServiceImpl implements StudentService {
    
    private final StudentRepository studentRepository;

    @Override
    @Transactional
    public StudentResponse create(StudentRequest req) {
        var student = StudentMapper.toEntity(req);
        studentRepository.save(student);
        return StudentMapper.toResponse(student);
    }

    @Override
    @Transactional(readOnly = true)
    public StudentResponse get(UUID id) {
        return studentRepository.findById(id)
                .map(StudentMapper::toResponse)
                .orElseThrow(() -> new NotFoundException("Student %s not found".formatted(id)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentResponse> list() {
        return studentRepository.findAll()
                .stream()
                .map(StudentMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public StudentResponse update(UUID id, StudentUpdateRequest req) {
        var entity = studentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                    "Student %s not found".formatted(id)));
        StudentMapper.patch(entity, req);
        var saved = studentRepository.save(entity);
        return StudentMapper.toResponse(saved);
    }

    @Override
    public void delete(UUID id) {
    if (!studentRepository.existsById(id)) {
        throw new NotFoundException("Student %s not found".formatted(id));
    }
    studentRepository.deleteById(id);
    }
}

