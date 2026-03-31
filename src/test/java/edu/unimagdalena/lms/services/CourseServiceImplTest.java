package edu.unimagdalena.lms.services;

import edu.unimagdalena.lms.api.dto.CourseDtos.*;
import edu.unimagdalena.lms.entities.Course;
import edu.unimagdalena.lms.entities.Instructor;
import edu.unimagdalena.lms.exception.NotFoundException;
import edu.unimagdalena.lms.repositories.CourseRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseServiceImplTest {

    @Mock
    CourseRepository courseRepository;

    @Mock
    InstructorRepository instructorRepository;

    @InjectMocks
    CourseServiceImpl courseService;

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

    private Course buildCourse(UUID id, Instructor instructor) {
        return Course.builder()
                .id(id)
                .title("Java Avanzado")
                .instructor(instructor)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }
    // ─────────────────────────────────────────────────────

    @Test
    @DisplayName("Crea un curso cuando el instructor existe")
    void shouldCreateCourseWhenInstructorExists() {
        // Arrange
        var instructorId = UUID.randomUUID();
        var instructor = buildInstructor(instructorId);
        var req = new CourseRequest("Java Avanzado", instructorId);

        when(instructorRepository.findById(instructorId)).thenReturn(Optional.of(instructor));
        when(courseRepository.save(any())).thenAnswer(inv -> {
            Course c = inv.getArgument(0);
            c.setId(UUID.randomUUID());
            return c;
        });

        // Act
        CourseResponse result = courseService.create(req);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.title()).isEqualTo("Java Avanzado");
        assertThat(result.instructorId()).isEqualTo(instructorId);
        verify(instructorRepository).findById(instructorId);
        verify(courseRepository).save(any(Course.class));
    }

    @Test
    @DisplayName("Lanza NotFoundException al crear curso con instructor inexistente")
    void shouldThrowNotFoundWhenInstructorNotExists() {
        // Arrange
        var instructorId = UUID.randomUUID();
        var req = new CourseRequest("Java Avanzado", instructorId);

        when(instructorRepository.findById(instructorId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> courseService.create(req))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining(instructorId.toString());
        verify(courseRepository, never()).save(any());
    }

    @Test
    @DisplayName("Retorna el curso cuando existe")
    void shouldReturnCourseWhenFound() {
        // Arrange
        var id = UUID.randomUUID();
        var instructor = buildInstructor(UUID.randomUUID());

        when(courseRepository.findById(id))
                .thenReturn(Optional.of(buildCourse(id, instructor)));

        // Act
        CourseResponse result = courseService.get(id);

        // Assert
        assertThat(result.id()).isEqualTo(id);
        assertThat(result.title()).isEqualTo("Java Avanzado");
    }

    @Test
    @DisplayName("Lanza NotFoundException cuando el curso no existe")
    void shouldThrowNotFoundWhenCourseNotFound() {
        // Arrange
        var id = UUID.randomUUID();
        when(courseRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> courseService.get(id))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining(id.toString());
    }

    @Test
    @DisplayName("Retorna lista de todos los cursos")
    void shouldReturnAllCourses() {
        // Arrange
        var instructor = buildInstructor(UUID.randomUUID());
        when(courseRepository.findAll()).thenReturn(List.of(
                buildCourse(UUID.randomUUID(), instructor),
                buildCourse(UUID.randomUUID(), instructor)
        ));

        // Act
        List<CourseResponse> result = courseService.list();

        // Assert
        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("Actualiza solo los campos enviados del curso")
    void shouldUpdateCourseFields() {
        // Arrange
        var id = UUID.randomUUID();
        var instructor = buildInstructor(UUID.randomUUID());
        var entity = buildCourse(id, instructor);

        when(courseRepository.findById(id)).thenReturn(Optional.of(entity));
        when(courseRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var req = new CourseUpdateRequest("Título Nuevo", null, null); // description null = no cambia

        // Act
        CourseResponse result = courseService.update(id, req);

        // Assert
        assertThat(result.title()).isEqualTo("Título Nuevo");
        verify(courseRepository).save(any(Course.class));
    }

    @Test
    @DisplayName("Lanza NotFoundException al actualizar curso inexistente")
    void shouldThrowNotFoundWhenUpdatingNonExistent() {
        // Arrange
        var id = UUID.randomUUID();
        when(courseRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> courseService.update(id, new CourseUpdateRequest(null, null, null)))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("Elimina el curso cuando existe")
    void shouldDeleteCourseWhenExists() {
        // Arrange
        var id = UUID.randomUUID();
        when(courseRepository.existsById(id)).thenReturn(true);

        // Act
        courseService.delete(id);

        // Assert
        verify(courseRepository).deleteById(id);
    }

    @Test
    @DisplayName("Lanza NotFoundException al eliminar curso inexistente")
    void shouldThrowNotFoundWhenDeletingNonExistent() {
        // Arrange
        var id = UUID.randomUUID();
        when(courseRepository.existsById(id)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> courseService.delete(id))
                .isInstanceOf(NotFoundException.class);
        verify(courseRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Actualiza status y active del curso")
    void shouldUpdateStatusAndActive() {
        var id = UUID.randomUUID();
        var instructor = buildInstructor(UUID.randomUUID());
        var entity = buildCourse(id, instructor);

        when(courseRepository.findById(id)).thenReturn(Optional.of(entity));
        when(courseRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var req = new CourseUpdateRequest(null, "PUBLISHED", false);

        CourseResponse result = courseService.update(id, req);

        assertThat(result.title()).isEqualTo("Java Avanzado"); // no cambió
        assertThat(result.status()).isEqualTo("PUBLISHED");
        assertThat(result.active()).isFalse();
    }
}