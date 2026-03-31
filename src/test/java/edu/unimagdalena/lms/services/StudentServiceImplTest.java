package edu.unimagdalena.lms.services;

import edu.unimagdalena.lms.api.dto.StudentDtos.*;
import edu.unimagdalena.lms.entities.Student;
import edu.unimagdalena.lms.exception.NotFoundException;
import edu.unimagdalena.lms.repositories.StudentRepository;
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
class StudentServiceImplTest {

        @Mock
        StudentRepository studentRepository;

        @InjectMocks
        StudentServiceImpl studentService;

    @Test
    @DisplayName("Crea un estudiante y retorna el response")
    void shouldCreateAndReturnResponse() {
        // Arrange
        var req = new StudentRequest("ana@test.com", "Ana García");

        when(studentRepository.save(any())).thenAnswer(inv -> {
            Student s = inv.getArgument(0);
            s.setId(UUID.randomUUID());  // simula que la BD asignó el ID
            return s;
        });

        // Act
        StudentResponse result = studentService.create(req);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.email()).isEqualTo("ana@test.com");
        assertThat(result.fullName()).isEqualTo("Ana García");
        assertThat(result.id()).isNotNull();  // confirma que tiene ID
        verify(studentRepository).save(any(Student.class));
    }

    @Test
    @DisplayName("Retorna el estudiante cuando existe")
    void shouldReturnStudentWhenFound() {
        // Arrange
        var id = UUID.randomUUID();
        var student = Student.builder()
                .id(id)
                .email("ana@test.com")
                .fullName("Ana García")
                .createdAt(Instant.now())
                .build();

        when(studentRepository.findById(id)).thenReturn(Optional.of(student));

        // Act
        StudentResponse result = studentService.get(id);

        // Assert
        assertThat(result.id()).isEqualTo(id);
        assertThat(result.email()).isEqualTo("ana@test.com");
    }

    @Test
    @DisplayName("Lanza NotFoundException cuando el estudiante no existe")
    void shouldThrowNotFoundWhenStudentNotFound() {
        // Arrange
        var id = UUID.randomUUID();
        when(studentRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> studentService.get(id))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining(id.toString());
    }

    @Test
    @DisplayName("Retorna lista de todos los estudiantes")
    void shouldReturnAllStudents() {
        // Arrange
        var student1 = Student.builder().id(UUID.randomUUID())
                .email("ana@test.com").fullName("Ana García")
                .createdAt(Instant.now()).build();
        var student2 = Student.builder().id(UUID.randomUUID())
                .email("juan@test.com").fullName("Juan Pérez")
                .createdAt(Instant.now()).build();

        when(studentRepository.findAll()).thenReturn(List.of(student1, student2));

        // Act
        List<StudentResponse> result = studentService.list();

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result).extracting(StudentResponse::email)
                .containsExactlyInAnyOrder("ana@test.com", "juan@test.com");
    }

    @Test
    @DisplayName("Actualiza solo los campos enviados")
    void shouldUpdateStudentFields() {
        // Arrange
        var id = UUID.randomUUID();
        var entity = Student.builder()
                .id(id)
                .email("viejo@test.com")
                .fullName("Nombre Viejo")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(studentRepository.findById(id)).thenReturn(Optional.of(entity));
        when(studentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var req = new StudentUpdateRequest("nuevo@test.com", null); // solo cambia email

        // Act
        StudentResponse result = studentService.update(id, req);

        // Assert
        assertThat(result.email()).isEqualTo("nuevo@test.com");
        assertThat(result.fullName()).isEqualTo("Nombre Viejo"); // no cambió
        verify(studentRepository).save(any(Student.class));
    }

    @Test
    @DisplayName("Lanza NotFoundException al actualizar estudiante inexistente")
    void shouldThrowNotFoundWhenUpdatingNonExistent() {
        // Arrange
        var id = UUID.randomUUID();
        when(studentRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> studentService.update(id, new StudentUpdateRequest(null, null)))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("Elimina el estudiante cuando existe")
    void shouldDeleteStudentWhenExists() {
        // Arrange
        var id = UUID.randomUUID();
        when(studentRepository.existsById(id)).thenReturn(true);

        // Act
        studentService.delete(id);

        // Assert
        verify(studentRepository).deleteById(id);
    }

    @Test
    @DisplayName("Lanza NotFoundException al eliminar estudiante inexistente")
    void shouldThrowNotFoundWhenDeletingNonExistent() {
        // Arrange
        var id = UUID.randomUUID();
        when(studentRepository.existsById(id)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> studentService.delete(id))
                .isInstanceOf(NotFoundException.class);

        verify(studentRepository, never()).deleteById(any());
        // ↑ confirma que deleteById NUNCA se llamó
    }
}
