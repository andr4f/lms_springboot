package edu.unimagdalena.lms.repositories;

import edu.unimagdalena.lms.entities.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class CourseRepositoryTest extends AbstractRepositoryIT {

    @Autowired CourseRepository courseRepository;
    @Autowired InstructorRepository instructorRepository;
    @Autowired EnrollmentRepository enrollmentRepository;
    @Autowired StudentRepository studentRepository;

    // ── helpers ──────────────────────────────────────────────
    private Instructor savedInstructor() {
        return instructorRepository.save(Instructor.builder()
                .email("prof@test.com")
                .fullName("Prof Test")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build());
    }

    private Course savedCourse(Instructor instructor, String title,
                                String status, boolean active) {
        return courseRepository.save(Course.builder()
                .title(title)
                .status(status)
                .active(active)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .instructor(instructor)
                .build());
    }
    // ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("Encuentra un curso por título exacto")
    void shouldFindByTitle() {
        // Given
        savedCourse(savedInstructor(), "Java Avanzado", "ACTIVE", true);

        // When
        Optional<Course> result = courseRepository.findByTitle("Java Avanzado");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getStatus()).isEqualTo("ACTIVE");
    }

    @Test
    @DisplayName("Retorna vacío cuando el título no existe")
    void shouldReturnEmptyWhenTitleNotFound() {
        // When
        Optional<Course> result = courseRepository.findByTitle("No existe");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Encuentra solo los cursos activos")
    void shouldFindByActiveTrue() {
        // Given
        var instructor = savedInstructor();
        savedCourse(instructor, "Java Avanzado", "ACTIVE", true);
        savedCourse(instructor, "Python Básico", "DRAFT",  false);

        // When
        List<Course> result = courseRepository.findByActiveTrue();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Java Avanzado");
    }

    @Test
    @DisplayName("Encuentra cursos por status")
    void shouldFindByStatus() {
        // Given
        var instructor = savedInstructor();
        savedCourse(instructor, "Java Avanzado", "ACTIVE",   true);
        savedCourse(instructor, "Python Básico", "DRAFT",    false);
        savedCourse(instructor, "Spring Boot",   "ACTIVE",   true);

        // When
        List<Course> result = courseRepository.findByStatus("ACTIVE");

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(c -> c.getStatus().equals("ACTIVE"));
    }

    @Test
    @DisplayName("Encuentra cursos por instructorId")
    void shouldFindByInstructorId() {
        // Given
        var instructor1 = savedInstructor();
        var instructor2 = instructorRepository.save(Instructor.builder()
                .email("otro@test.com").fullName("Otro Profesor")
                .createdAt(Instant.now()).updatedAt(Instant.now()).build());

        savedCourse(instructor1, "Java",   "ACTIVE", true);
        savedCourse(instructor1, "Spring", "ACTIVE", true);
        savedCourse(instructor2, "Python", "ACTIVE", true);

        // When
        List<Course> result = courseRepository.findByInstructorId(instructor1.getId());

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(c -> c.getInstructor().getId()
                .equals(instructor1.getId()));
    }

    @Test
    @DisplayName("Encuentra cursos creados después de una fecha")
    void shouldFindByCreatedAtAfter() {
        // Given
        var instructor = savedInstructor();
        savedCourse(instructor, "Java Avanzado", "ACTIVE", true);

        // When
        List<Course> result = courseRepository
                .findByCreatedAtAfter(Instant.now().minusSeconds(60));

        // Then
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("Encuentra solo cursos activos de un instructor específico")
    void shouldFindActiveCoursesByInstructor() {
        // Given
        var instructor = savedInstructor();
        savedCourse(instructor, "Java Avanzado", "ACTIVE", true);
        savedCourse(instructor, "Python Básico", "DRAFT",  false); // inactivo

        // When
        List<Course> result = courseRepository
                .findActiveCoursesByInstructor(instructor.getId());

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getActive()).isTrue();
    }

    @Test
    @DisplayName("Encuentra cursos por palabra clave en el título ignorando mayúsculas")
    void shouldFindByTitleContainingIgnoreCase() {
        // Given
        var instructor = savedInstructor();
        savedCourse(instructor, "Java Avanzado", "ACTIVE", true);
        savedCourse(instructor, "JAVA Básico",   "ACTIVE", true);
        savedCourse(instructor, "Python Intro",  "ACTIVE", true);

        // When
        List<Course> result = courseRepository
                .findByTitleContainingIgnoreCase("java");

        // Then
        assertThat(result).hasSize(2); // "Java Avanzado" y "JAVA Básico"
        assertThat(result).allMatch(c -> c.getTitle().toLowerCase().contains("java"));
    }

    @Test
    @DisplayName("Cuenta matrículas de un curso")
    void shouldCountEnrollmentsByCourse() {
        // Given
        var instructor = savedInstructor();
        var course = savedCourse(instructor, "Java", "ACTIVE", true);
        var student = studentRepository.save(Student.builder()
                .email("ana@test.com").fullName("Ana García")
                .createdAt(Instant.now()).updatedAt(Instant.now()).build());

        enrollmentRepository.save(Enrollment.builder()
                .student(student).course(course)
                .status("ACTIVE").enrolledAt(Instant.now()).build());

        // When
        Object[] result = courseRepository.countEnrollmentsByCourse(course.getId());

        // Then
        assertThat(result).isNotNull();
    }
}
