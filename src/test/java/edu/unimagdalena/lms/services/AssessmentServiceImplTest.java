package edu.unimagdalena.lms.services;

import edu.unimagdalena.lms.api.dto.AssessmentDtos.*;
import edu.unimagdalena.lms.entities.Assessment;
import edu.unimagdalena.lms.entities.Course;
import edu.unimagdalena.lms.entities.Instructor;
import edu.unimagdalena.lms.entities.Student;
import edu.unimagdalena.lms.exception.NotFoundException;
import edu.unimagdalena.lms.repositories.AssessmentRepository;
import edu.unimagdalena.lms.repositories.CourseRepository;
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
class AssessmentServiceImplTest {

    @Mock
    AssessmentRepository assessmentRepository;

    @Mock
    StudentRepository studentRepository;

    @Mock
    CourseRepository courseRepository;

    @InjectMocks
    AssessmentServiceImpl assessmentService;

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

    private Assessment buildAssessment(UUID id, Student student, Course course) {
        return Assessment.builder()
                .id(id)
                .type("QUIZ")
                .score(85)
                .takenAt(Instant.now())
                .student(student)
                .course(course)
                .build();
    }
    // ─────────────────────────────────────────────────────

    @Test
    @DisplayName("Crea un assessment cuando el estudiante y curso existen")
    void shouldCreateAssessmentWhenStudentAndCourseExist() {
        // Arrange
        var studentId = UUID.randomUUID();
        var courseId = UUID.randomUUID();
        var student = buildStudent(studentId);
        var course = buildCourse(courseId);
        var req = new AssessmentRequest(studentId, courseId, 90, "EXAM");

        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(assessmentRepository.save(any())).thenAnswer(inv -> {
            Assessment a = inv.getArgument(0);
            a.setId(UUID.randomUUID());
            return a;
        });

        // Act
        AssessmentResponse result = assessmentService.create(req);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.score()).isEqualTo(90);
        assertThat(result.type()).isEqualTo("EXAM");
        assertThat(result.studentId()).isEqualTo(studentId);
        assertThat(result.courseId()).isEqualTo(courseId);
        verify(studentRepository).findById(studentId);
        verify(courseRepository).findById(courseId);
        verify(assessmentRepository).save(any(Assessment.class));
    }

    @Test
    @DisplayName("Lanza NotFoundException al crear assessment con estudiante inexistente")
    void shouldThrowNotFoundWhenStudentNotExists() {
        // Arrange
        var studentId = UUID.randomUUID();
        var req = new AssessmentRequest(studentId, UUID.randomUUID(), 90, "EXAM");

        when(studentRepository.findById(studentId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> assessmentService.create(req))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining(studentId.toString());
        verify(assessmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Lanza NotFoundException al crear assessment con curso inexistente")
    void shouldThrowNotFoundWhenCourseNotExists() {
        // Arrange
        var studentId = UUID.randomUUID();
        var courseId = UUID.randomUUID();
        var req = new AssessmentRequest(studentId, courseId, 90, "EXAM");

        when(studentRepository.findById(studentId)).thenReturn(Optional.of(buildStudent(studentId)));
        when(courseRepository.findById(courseId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> assessmentService.create(req))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining(courseId.toString());
        verify(assessmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Retorna el assessment cuando existe")
    void shouldReturnAssessmentWhenFound() {
        // Arrange
        var id = UUID.randomUUID();
        var student = buildStudent(UUID.randomUUID());
        var course = buildCourse(UUID.randomUUID());

        when(assessmentRepository.findById(id))
                .thenReturn(Optional.of(buildAssessment(id, student, course)));

        // Act
        AssessmentResponse result = assessmentService.get(id);

        // Assert
        assertThat(result.id()).isEqualTo(id);
        assertThat(result.type()).isEqualTo("QUIZ");
        assertThat(result.score()).isEqualTo(85);
    }

    @Test
    @DisplayName("Lanza NotFoundException cuando el assessment no existe")
    void shouldThrowNotFoundWhenAssessmentNotFound() {
        // Arrange
        var id = UUID.randomUUID();
        when(assessmentRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> assessmentService.get(id))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining(id.toString());
    }

    @Test
    @DisplayName("Retorna lista de todos los assessments")
    void shouldReturnAllAssessments() {
        // Arrange
        var student = buildStudent(UUID.randomUUID());
        var course = buildCourse(UUID.randomUUID());
        when(assessmentRepository.findAll()).thenReturn(List.of(
                buildAssessment(UUID.randomUUID(), student, course),
                buildAssessment(UUID.randomUUID(), student, course),
                buildAssessment(UUID.randomUUID(), student, course)));

        // Act
        List<AssessmentResponse> result = assessmentService.list();

        // Assert
        assertThat(result).hasSize(3);
    }

    @Test
    @DisplayName("Retorna assessments filtrados por estudiante")
    void shouldReturnAssessmentsByStudent() {
        // Arrange
        var studentId = UUID.randomUUID();
        var student = buildStudent(studentId);
        var course = buildCourse(UUID.randomUUID());
        when(assessmentRepository.findByStudentId(studentId)).thenReturn(List.of(
                buildAssessment(UUID.randomUUID(), student, course),
                buildAssessment(UUID.randomUUID(), student, course)));

        // Act
        List<AssessmentResponse> result = assessmentService.listByStudent(studentId);

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(a -> a.studentId().equals(studentId));
    }

    @Test
    @DisplayName("Retorna assessments filtrados por curso")
    void shouldReturnAssessmentsByCourse() {
        // Arrange
        var courseId = UUID.randomUUID();
        var student = buildStudent(UUID.randomUUID());
        var course = buildCourse(courseId);
        when(assessmentRepository.findByCourseId(courseId)).thenReturn(List.of(
                buildAssessment(UUID.randomUUID(), student, course)));

        // Act
        List<AssessmentResponse> result = assessmentService.listByCourse(courseId);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result).allMatch(a -> a.courseId().equals(courseId));
    }

    @Test
    @DisplayName("Actualiza el score del assessment")
    void shouldUpdateAssessmentScore() {
        // Arrange
        var id = UUID.randomUUID();
        var student = buildStudent(UUID.randomUUID());
        var course = buildCourse(UUID.randomUUID());
        var entity = buildAssessment(id, student, course);

        when(assessmentRepository.findById(id)).thenReturn(Optional.of(entity));
        when(assessmentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var req = new AssessmentUpdateRequest(95, null);

        // Act
        AssessmentResponse result = assessmentService.update(id, req);

        // Assert
        assertThat(result.score()).isEqualTo(95);
        assertThat(result.type()).isEqualTo("QUIZ"); // no cambió
        verify(assessmentRepository).save(any(Assessment.class));
    }

    @Test
    @DisplayName("Actualiza el type del assessment")
    void shouldUpdateAssessmentType() {
        // Arrange
        var id = UUID.randomUUID();
        var student = buildStudent(UUID.randomUUID());
        var course = buildCourse(UUID.randomUUID());
        var entity = buildAssessment(id, student, course);

        when(assessmentRepository.findById(id)).thenReturn(Optional.of(entity));
        when(assessmentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var req = new AssessmentUpdateRequest(null, "FINAL_EXAM");

        // Act
        AssessmentResponse result = assessmentService.update(id, req);

        // Assert
        assertThat(result.score()).isEqualTo(85); // no cambió
        assertThat(result.type()).isEqualTo("FINAL_EXAM");
    }

    @Test
    @DisplayName("Lanza NotFoundException al actualizar assessment inexistente")
    void shouldThrowNotFoundWhenUpdatingNonExistent() {
        // Arrange
        var id = UUID.randomUUID();
        when(assessmentRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> assessmentService.update(id, new AssessmentUpdateRequest(null, null)))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("Elimina el assessment cuando existe")
    void shouldDeleteAssessmentWhenExists() {
        // Arrange
        var id = UUID.randomUUID();
        when(assessmentRepository.existsById(id)).thenReturn(true);

        // Act
        assessmentService.delete(id);

        // Assert
        verify(assessmentRepository).deleteById(id);
    }

    @Test
    @DisplayName("Lanza NotFoundException al eliminar assessment inexistente")
    void shouldThrowNotFoundWhenDeletingNonExistent() {
        // Arrange
        var id = UUID.randomUUID();
        when(assessmentRepository.existsById(id)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> assessmentService.delete(id))
                .isInstanceOf(NotFoundException.class);
        verify(assessmentRepository, never()).deleteById(any());
    }
}
