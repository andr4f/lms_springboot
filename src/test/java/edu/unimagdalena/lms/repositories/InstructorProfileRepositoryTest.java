package edu.unimagdalena.lms.repositories;

import edu.unimagdalena.lms.entities.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class InstructorProfileRepositoryTest extends AbstractRepositoryIT {

    @Autowired InstructorProfileRepository instructorProfileRepository;
    @Autowired InstructorRepository instructorRepository;

    // ── helpers ──────────────────────────────────────────────
    private Instructor savedInstructor() {
        return instructorRepository.save(Instructor.builder()
                .email("carlos@test.com")
                .fullName("Carlos Ruiz")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build());
    }

    private InstructorProfile savedProfile(Instructor instructor) {
        return instructorProfileRepository.save(InstructorProfile.builder()
                .phone("3001234567")
                .bio("Profesor de Java")
                .instructor(instructor)
                .build());
    }
    // ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("Encuentra el perfil de un instructor por instructorId")
    void shouldFindByInstructorId() {
        // Given
        var instructor = savedInstructor();
        var profile = savedProfile(instructor);

        // When
        Optional<InstructorProfile> result = instructorProfileRepository
                .findByInstructorId(instructor.getId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(profile.getId());
        assertThat(result.get().getPhone()).isEqualTo("3001234567");
    }

    @Test
    @DisplayName("Retorna vacío cuando el instructorId no tiene perfil")
    void shouldReturnEmptyWhenInstructorHasNoProfile() {
        // Given — instructor sin perfil
        var instructor = savedInstructor();

        // When
        Optional<InstructorProfile> result = instructorProfileRepository
                .findByInstructorId(instructor.getId());

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Encuentra el perfil por número de teléfono")
    void shouldFindByPhone() {
        // Given
        var instructor = savedInstructor();
        savedProfile(instructor);

        // When
        Optional<InstructorProfile> result = instructorProfileRepository
                .findByPhone("3001234567");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getBio()).isEqualTo("Profesor de Java");
    }

    @Test
    @DisplayName("Retorna vacío cuando el teléfono no existe")
    void shouldReturnEmptyWhenPhoneNotFound() {
        // When
        Optional<InstructorProfile> result = instructorProfileRepository
                .findByPhone("0000000000");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Carga el perfil junto con el instructor usando JOIN FETCH")
    void shouldFindProfileWithInstructor() {
        // Given
        var instructor = savedInstructor();
        savedProfile(instructor);

        // When
        Optional<InstructorProfile> result = instructorProfileRepository
                .findProfileWithInstructor(instructor.getId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getInstructor()).isNotNull();
        assertThat(result.get().getInstructor().getFullName()).isEqualTo("Carlos Ruiz");
    }
}
