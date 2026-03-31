package edu.unimagdalena.lms.services;

import edu.unimagdalena.lms.api.dto.InstructorDtos.*;
import edu.unimagdalena.lms.exception.NotFoundException;
import edu.unimagdalena.lms.repositories.InstructorRepository;
import edu.unimagdalena.lms.services.mapper.InstructorMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class InstructorServiceImpl implements InstructorService {

    private final InstructorRepository instructorRepository;

    @Override
    @Transactional
    public InstructorResponse create(InstructorRequest req) {
        var entity = InstructorMapper.toEntity(req);
        if (entity.getProfile() != null) {
            entity.getProfile().setInstructor(entity); // ← enlaza el perfil al instructor
        }
        var saved = instructorRepository.save(entity);
        return InstructorMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public InstructorResponse get(UUID id) {
        return instructorRepository.findById(id)
                .map(InstructorMapper::toResponse)
                .orElseThrow(() -> new NotFoundException(
                    "Instructor %s not found".formatted(id)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<InstructorResponse> list() {
        return instructorRepository.findAll()
                .stream()
                .map(InstructorMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public InstructorResponse update(UUID id, InstructorUpdateRequest req) {
        var entity = instructorRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                    "Instructor %s not found".formatted(id)));
        InstructorMapper.patch(entity, req);
        var saved = instructorRepository.save(entity);
        return InstructorMapper.toResponse(saved);
    }

    @Override
    public void delete(UUID id) {
        if (!instructorRepository.existsById(id)) {
            throw new NotFoundException("Instructor %s not found".formatted(id));
        }
        instructorRepository.deleteById(id);
    }
}