package edu.unimagdalena.lms.services;

import edu.unimagdalena.lms.api.dto.EnrollmentDtos.*;
import edu.unimagdalena.lms.entities.Course;
import edu.unimagdalena.lms.entities.Enrollment;
import edu.unimagdalena.lms.entities.Instructor;
import edu.unimagdalena.lms.entities.Student;
import edu.unimagdalena.lms.exception.NotFoundException;
import edu.unimagdalena.lms.repositories.CourseRepository;
import edu.unimagdalena.lms.repositories.EnrollmentRepository;
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
class EnrollmentServiceImplTest {

    @Mock
    EnrollmentRepository enrollmentRepository;

    @Mock
    StudentRepository studentRepository;

    @Mock
    CourseRepository courseRepository;

    @InjectMocks
    EnrollmentServiceImpl enrollmentService;

    // ── helpers ──────────────────────────────────────────
    private Student buildStudent(UUID id) {
        return Student.builder()
                .id(id)
                .email("student@test.com")
                .fullName("Student Test")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    private Instructor buildInstructor() {
        return Instructor.builder()
                .id(UUID.randomUUID())
                .email("prof@test.com")
                .fullName("Prof Test")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    private Course buildCourse(UUID id) {
        return Course.builder()
                .id(id)
                .title("Java Avanzado")
                .status("DRAFT")
                .active(true)
                .instructor(buildInstructor())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    private Enrollment buildEnrollment(UUID id, Student student, Course course) {
        return Enrollment.builder()
                .id(id)
                .status("ACTIVE")
                .enrolledAt(Instant.now())
                .student(student)
                .course(course)
                .build();
    }
    // ─────────────────────────────────────────────────────

    @Test
    @DisplayName("Crea un enrollment cuando el estudiante y curso existen")
    void shouldCreateEnrollmentWhenStudentAndCourseExist() {
        // Arrange
        var studentId = UUID.randomUUID();
        var courseId = UUID.randomUUID();
        var student = buildStudent(studentId);
        var course = buildCourse(courseId);
        var req = new EnrollmentRequest(studentId, courseId);

        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(enrollmentRepository.save(any())).thenAnswer(inv -> {
            Enrollment e = inv.getArgument(0);
            e.setId(UUID.randomUUID());
            return e;
        });

        // Act
        EnrollmentResponse result = enrollmentService.create(req);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.status()).isEqualTo("ACTIVE");
        assertThat(result.studentId()).isEqualTo(studentId);
        assertThat(result.courseId()).isEqualTo(courseId);
        verify(studentRepository).findById(studentId);
        verify(courseRepository).findById(courseId);
        verify(enrollmentRepository).save(any(Enrollment.class));
    }

    @Test
    @DisplayName("Lanza NotFoundException al crear enrollment con estudiante inexistente")
    void shouldThrowNotFoundWhenStudentNotExists() {
        // Arrange
        var studentId = UUID.randomUUID();
        var req = new EnrollmentRequest(studentId, UUID.randomUUID());

        when(studentRepository.findById(studentId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> enrollmentService.create(req))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining(studentId.toString());
        verify(enrollmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Lanza NotFoundException al crear enrollment con curso inexistente")
    void shouldThrowNotFoundWhenCourseNotExists() {
        // Arrange
        var studentId = UUID.randomUUID();
        var courseId = UUID.randomUUID();
        var req = new EnrollmentRequest(studentId, courseId);

        when(studentRepository.findById(studentId)).thenReturn(Optional.of(buildStudent(studentId)));
        when(courseRepository.findById(courseId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> enrollmentService.create(req))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining(courseId.toString());
        verify(enrollmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Retorna el enrollment cuando existe")
    void shouldReturnEnrollmentWhenFound() {
        // Arrange
        var id = UUID.randomUUID();
        var student = buildStudent(UUID.randomUUID());
        var course = buildCourse(UUID.randomUUID());

        when(enrollmentRepository.findById(id))
                .thenReturn(Optional.of(buildEnrollment(id, student, course)));

        // Act
        EnrollmentResponse result = enrollmentService.get(id);

        // Assert
        assertThat(result.id()).isEqualTo(id);
        assertThat(result.status()).isEqualTo("ACTIVE");
    }

    @Test
    @DisplayName("Lanza NotFoundException cuando el enrollment no existe")
    void shouldThrowNotFoundWhenEnrollmentNotFound() {
        // Arrange
        var id = UUID.randomUUID();
        when(enrollmentRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> enrollmentService.get(id))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining(id.toString());
    }

    @Test
    @DisplayName("Retorna lista de todos los enrollments")
    void shouldReturnAllEnrollments() {
        // Arrange
        var student = buildStudent(UUID.randomUUID());
        var course = buildCourse(UUID.randomUUID());
        when(enrollmentRepository.findAll()).thenReturn(List.of(
                buildEnrollment(UUID.randomUUID(), student, course),
                buildEnrollment(UUID.randomUUID(), student, course)));

        // Act
        List<EnrollmentResponse> result = enrollmentService.list();

        // Assert
        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("Retorna enrollments filtrados por estudiante")
    void shouldReturnEnrollmentsByStudent() {
        // Arrange
        var studentId = UUID.randomUUID();
        var student = buildStudent(studentId);
        var course = buildCourse(UUID.randomUUID());
        when(enrollmentRepository.findByStudentId(studentId)).thenReturn(List.of(
                buildEnrollment(UUID.randomUUID(), student, course),
                buildEnrollment(UUID.randomUUID(), student, course)));

        // Act
        List<EnrollmentResponse> result = enrollmentService.listByStudent(studentId);

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(e -> e.studentId().equals(studentId));
    }

    @Test
    @DisplayName("Retorna enrollments filtrados por curso")
    void shouldReturnEnrollmentsByCourse() {
        // Arrange
        var courseId = UUID.randomUUID();
        var student = buildStudent(UUID.randomUUID());
        var course = buildCourse(courseId);
        when(enrollmentRepository.findByCourseId(courseId)).thenReturn(List.of(
                buildEnrollment(UUID.randomUUID(), student, course)));

        // Act
        List<EnrollmentResponse> result = enrollmentService.listByCourse(courseId);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result).allMatch(e -> e.courseId().equals(courseId));
    }

    @Test
    @DisplayName("Actualiza el status del enrollment")
    void shouldUpdateEnrollmentStatus() {
        // Arrange
        var id = UUID.randomUUID();
        var student = buildStudent(UUID.randomUUID());
        var course = buildCourse(UUID.randomUUID());
        var entity = buildEnrollment(id, student, course);

        when(enrollmentRepository.findById(id)).thenReturn(Optional.of(entity));
        when(enrollmentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var req = new EnrollmentUpdateRequest("COMPLETED");

        // Act
        EnrollmentResponse result = enrollmentService.update(id, req);

        // Assert
        assertThat(result.status()).isEqualTo("COMPLETED");
        verify(enrollmentRepository).save(any(Enrollment.class));
    }

    @Test
    @DisplayName("No cambia el status si es null en el update")
    void shouldNotUpdateStatusWhenNull() {
        // Arrange
        var id = UUID.randomUUID();
        var student = buildStudent(UUID.randomUUID());
        var course = buildCourse(UUID.randomUUID());
        var entity = buildEnrollment(id, student, course);

        when(enrollmentRepository.findById(id)).thenReturn(Optional.of(entity));
        when(enrollmentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var req = new EnrollmentUpdateRequest(null);

        // Act
        EnrollmentResponse result = enrollmentService.update(id, req);

        // Assert
        assertThat(result.status()).isEqualTo("ACTIVE"); // no cambió
    }

    @Test
    @DisplayName("Lanza NotFoundException al actualizar enrollment inexistente")
    void shouldThrowNotFoundWhenUpdatingNonExistent() {
        // Arrange
        var id = UUID.randomUUID();
        when(enrollmentRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> enrollmentService.update(id, new EnrollmentUpdateRequest(null)))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("Elimina el enrollment cuando existe")
    void shouldDeleteEnrollmentWhenExists() {
        // Arrange
        var id = UUID.randomUUID();
        when(enrollmentRepository.existsById(id)).thenReturn(true);

        // Act
        enrollmentService.delete(id);

        // Assert
        verify(enrollmentRepository).deleteById(id);
    }

    @Test
    @DisplayName("Lanza NotFoundException al eliminar enrollment inexistente")
    void shouldThrowNotFoundWhenDeletingNonExistent() {
        // Arrange
        var id = UUID.randomUUID();
        when(enrollmentRepository.existsById(id)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> enrollmentService.delete(id))
                .isInstanceOf(NotFoundException.class);
        verify(enrollmentRepository, never()).deleteById(any());
    }
}
