package edu.unimagdalena.lms.services;

import edu.unimagdalena.lms.api.dto.InstructorDtos.*;
import edu.unimagdalena.lms.entities.Instructor;
import edu.unimagdalena.lms.entities.InstructorProfile;
import edu.unimagdalena.lms.exception.NotFoundException;
import edu.unimagdalena.lms.repositories.InstructorRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InstructorServiceImplTest {

    @Mock
    InstructorRepository instructorRepository;

    @InjectMocks
    InstructorServiceImpl instructorService;

    // ── helpers ──────────────────────────────────────────
    private Instructor buildInstructor(UUID id) {
        return Instructor.builder()
                .id(id)
                .email("prof@test.com")
                .fullName("Prof Test")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    private Instructor buildInstructorWithProfile(UUID id) {
        var profile = InstructorProfile.builder()
                .id(UUID.randomUUID())
                .phone("+57-300")
                .bio("Experto en Java")
                .build();
        var instructor = buildInstructor(id);
        instructor.setProfile(profile);
        profile.setInstructor(instructor);
        return instructor;
    }
    // ─────────────────────────────────────────────────────

    @Test
    @DisplayName("Crea un instructor sin perfil y retorna el response")
    void shouldCreateInstructorWithoutProfile() {
        // Arrange
        var req = new InstructorRequest("prof@test.com", "Prof Test", null);

        when(instructorRepository.save(any())).thenAnswer(inv -> {
            Instructor i = inv.getArgument(0);
            i.setId(UUID.randomUUID());
            return i;
        });

        // Act
        InstructorResponse result = instructorService.create(req);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.email()).isEqualTo("prof@test.com");
        assertThat(result.profile()).isNull();
        verify(instructorRepository).save(any(Instructor.class));
    }

    @Test
    @DisplayName("Crea un instructor con perfil y retorna el response con perfil")
    void shouldCreateInstructorWithProfile() {
        // Arrange
        var profileDto = new InstructorProfileDto("+57-300", "Experto en Java");
        var req = new InstructorRequest("prof@test.com", "Prof Test", profileDto);

        when(instructorRepository.save(any())).thenAnswer(inv -> {
            Instructor i = inv.getArgument(0);
            i.setId(UUID.randomUUID());
            return i;
        });

        // Act
        InstructorResponse result = instructorService.create(req);

        // Assert
        assertThat(result.profile()).isNotNull();
        assertThat(result.profile().phone()).isEqualTo("+57-300");
        assertThat(result.profile().bio()).isEqualTo("Experto en Java");
    }

    @Test
    @DisplayName("Retorna el instructor cuando existe")
    void shouldReturnInstructorWhenFound() {
        // Arrange
        var id = UUID.randomUUID();
        when(instructorRepository.findById(id))
                .thenReturn(Optional.of(buildInstructor(id)));

        // Act
        InstructorResponse result = instructorService.get(id);

        // Assert
        assertThat(result.id()).isEqualTo(id);
        assertThat(result.email()).isEqualTo("prof@test.com");
    }

    @Test
    @DisplayName("Lanza NotFoundException cuando el instructor no existe")
    void shouldThrowNotFoundWhenInstructorNotFound() {
        // Arrange
        var id = UUID.randomUUID();
        when(instructorRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> instructorService.get(id))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining(id.toString());
    }

    @Test
    @DisplayName("Retorna lista de todos los instructores")
    void shouldReturnAllInstructors() {
        // Arrange
        var id1 = UUID.randomUUID();
        var id2 = UUID.randomUUID();
        when(instructorRepository.findAll())
                .thenReturn(List.of(buildInstructor(id1), buildInstructor(id2)));

        // Act
        List<InstructorResponse> result = instructorService.list();

        // Assert
        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("Actualiza solo los campos enviados del instructor")
    void shouldUpdateInstructorFields() {
        // Arrange
        var id = UUID.randomUUID();
        var entity = buildInstructorWithProfile(id);

        when(instructorRepository.findById(id)).thenReturn(Optional.of(entity));
        when(instructorRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // solo cambia email, fullName null = no cambia
        var req = new InstructorUpdateRequest("nuevo@test.com", null, null, Instant.now());

        // Act
        InstructorResponse result = instructorService.update(id, req);

        // Assert
        assertThat(result.email()).isEqualTo("nuevo@test.com");
        assertThat(result.fullName()).isEqualTo("Prof Test"); // no cambió
        verify(instructorRepository).save(any(Instructor.class));
    }

    @Test
    @DisplayName("Actualiza el perfil del instructor")
    void shouldUpdateInstructorProfile() {
        // Arrange
        var id = UUID.randomUUID();
        var entity = buildInstructorWithProfile(id);

        when(instructorRepository.findById(id)).thenReturn(Optional.of(entity));
        when(instructorRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var req = new InstructorUpdateRequest(null, null,
                new InstructorProfileDto(null, "Nueva bio"), Instant.now());

        // Act
        InstructorResponse result = instructorService.update(id, req);

        // Assert
        assertThat(result.profile().bio()).isEqualTo("Nueva bio");
        assertThat(result.profile().phone()).isEqualTo("+57-300"); // no cambió
    }

    @Test
    @DisplayName("Lanza NotFoundException al actualizar instructor inexistente")
    void shouldThrowNotFoundWhenUpdatingNonExistent() {
        // Arrange
        var id = UUID.randomUUID();
        when(instructorRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> instructorService.update(id,
                new InstructorUpdateRequest(null, null, null, Instant.now())))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("Elimina el instructor cuando existe")
    void shouldDeleteInstructorWhenExists() {
        // Arrange
        var id = UUID.randomUUID();
        when(instructorRepository.existsById(id)).thenReturn(true);

        // Act
        instructorService.delete(id);

        // Assert
        verify(instructorRepository).deleteById(id);
    }

    @Test
    @DisplayName("Lanza NotFoundException al eliminar instructor inexistente")
    void shouldThrowNotFoundWhenDeletingNonExistent() {
        // Arrange
        var id = UUID.randomUUID();
        when(instructorRepository.existsById(id)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> instructorService.delete(id))
                .isInstanceOf(NotFoundException.class);
        verify(instructorRepository, never()).deleteById(any());
    }
}