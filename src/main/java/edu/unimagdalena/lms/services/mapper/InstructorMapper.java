package edu.unimagdalena.lms.services.mapper;

import edu.unimagdalena.lms.entities.Instructor;
import edu.unimagdalena.lms.entities.InstructorProfile;

import java.time.Instant;

import edu.unimagdalena.lms.api.dto.InstructorDtos.*;

public class InstructorMapper {
    
    public static Instructor toEntity(InstructorRequest req) {
        var profile = req.profile() == null ? null :
            InstructorProfile.builder()
                .phone(req.profile().phone())
                .bio(req.profile().bio())
                .build();

        return Instructor.builder()
                .email(req.email())
                .fullName(req.fullName())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .profile(profile)
                .build();
    }

    public static void patch(Instructor entity, InstructorUpdateRequest req) {
        if (req.email() != null) entity.setEmail(req.email());
        if (req.fullName() != null) entity.setFullName(req.fullName());
        entity.setUpdatedAt(Instant.now()); // <- siempre actualiza la fecha
        // No actual "updatedAt" field in Instructor, but if there were, we'd set it here

        if (req.profile() != null) {
            var p = entity.getProfile();
            if (p == null) {
                p = new InstructorProfile();
                entity.setProfile(p);
            }
            if (req.profile().phone() != null) p.setPhone(req.profile().phone());
            if (req.profile().bio() != null) p.setBio(req.profile().bio());
        }

    }

    public static InstructorResponse toResponse(Instructor instructor) {

        var p = instructor.getProfile();
        var profileDto = p == null ? null :
            new InstructorProfileDto(
                p.getPhone(),
                p.getBio()
            );

        return new InstructorResponse(
                instructor.getId(),
                instructor.getEmail(),
                instructor.getFullName(),
                profileDto,
                instructor.getCreatedAt()
        );
    }

}
