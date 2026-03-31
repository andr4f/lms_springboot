package edu.unimagdalena.lms.repositories;

import edu.unimagdalena.lms.entities.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class InstructorRepositoryTest extends AbstractRepositoryIT {

    @Autowired InstructorRepository instructorRepository;
    @Autowired CourseRepository courseRepository;
    @Autowired InstructorProfileRepository instructorProfileRepository;

    // ── helpers ──────────────────────────────────────────────
    private Instructor savedInstructor(String email, String fullName) {
        return instructorRepository.save(Instructor.builder()
                .email(email)
                .fullName(fullName)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build());
    }

    private Course savedCourse(Instructor instructor, boolean active) {
        return courseRepository.save(Course.builder()
                .title("Course " + active)
                .status("ACTIVE")
                .active(active)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .instructor(instructor)
                .build());
    }
    // ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("Encuentra un instructor por email exacto")
    void shouldFindByEmail() {
        // Given
        savedInstructor("carlos@test.com", "Carlos Ruiz");

        // When
        Optional<Instructor> result = instructorRepository.findByEmail("carlos@test.com");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getFullName()).isEqualTo("Carlos Ruiz");
    }

    @Test
    @DisplayName("Retorna vacío cuando el email no existe")
    void shouldReturnEmptyWhenEmailNotFound() {
        // When
        Optional<Instructor> result = instructorRepository.findByEmail("noexiste@test.com");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Encuentra un instructor por nombre exacto")
    void shouldFindByFullName() {
        // Given
        savedInstructor("carlos@test.com", "Carlos Ruiz");

        // When
        Optional<Instructor> result = instructorRepository.findByFullName("Carlos Ruiz");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("carlos@test.com");
    }

    @Test
    @DisplayName("Encuentra instructores por nombre parcial ignorando mayúsculas")
    void shouldFindByFullNameContainingIgnoreCase() {
        // Given
        savedInstructor("carlos@test.com", "Carlos Ruiz");
        savedInstructor("carmen@test.com", "Carmen López");
        savedInstructor("juan@test.com", "Juan Pérez");

        // When
        List<Instructor> result = instructorRepository
                .findByFullNameContainingIgnoreCase("car");

        // Then
        assertThat(result).hasSize(2); // "Carlos" y "Carmen" contienen "car"
    }

    @Test
    @DisplayName("Encuentra instructores que tienen al menos un curso activo")
    void shouldFindInstructorsWithActiveCourses() {
        // Given
        var instructor1 = savedInstructor("carlos@test.com", "Carlos Ruiz");
        var instructor2 = savedInstructor("juan@test.com", "Juan Pérez");

        savedCourse(instructor1, true);   // instructor1 tiene curso activo
        savedCourse(instructor2, false);  // instructor2 solo tiene curso inactivo

        // When
        List<Instructor> result = instructorRepository.findInstructorsWithActiveCourses();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEmail()).isEqualTo("carlos@test.com");
    }

    @Test
@DisplayName("Carga el perfil del instructor con fetch join")
void shouldFindByIdWithProfile() {
    // Given
    var instructor = savedInstructor("carlos@test.com", "Carlos Ruiz");

    var profile = instructorProfileRepository.save(
            InstructorProfile.builder()
                    .phone("3001234567")
                    .bio("Profesor de Java")
                    .instructor(instructor)
                    .build()
    );

    // mantener ambos lados de la relación sincronizados
    instructor.setProfile(profile);

    // When
    Optional<Instructor> result =
            instructorRepository.findByIdWithProfile(instructor.getId());

    // Then
    assertThat(result).isPresent();
    assertThat(result.get().getProfile()).isNotNull();
    assertThat(result.get().getProfile().getPhone()).isEqualTo("3001234567");
}
    @Test
    @DisplayName("Cuenta correctamente los cursos de un instructor")
    void shouldCountCoursesByInstructor() {
        // Given
        var instructor = savedInstructor("carlos@test.com", "Carlos Ruiz");
        savedCourse(instructor, true);
        savedCourse(instructor, false);
        savedCourse(instructor, true);

        // When
        Long count = instructorRepository.countCoursesByInstructor(instructor.getId());

        // Then
        assertThat(count).isEqualTo(3L);
    }
}
