package edu.unimagdalena.lms.repositories;

import edu.unimagdalena.lms.entities.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class EnrollmentRepositoryTest extends AbstractRepositoryIT {

    @Autowired EnrollmentRepository enrollmentRepository;
    @Autowired StudentRepository studentRepository;
    @Autowired CourseRepository courseRepository;
    @Autowired InstructorRepository instructorRepository;

    // ── helpers ──────────────────────────────────────────────
    private Student savedStudent() {
        return studentRepository.save(Student.builder()
                .email("ana@test.com")
                .fullName("Ana García")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build());
    }

    private Instructor savedInstructor() {
        return instructorRepository.save(Instructor.builder()
                .email("prof@test.com")
                .fullName("Prof Test")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build());
    }

    private Course savedCourse(Instructor instructor) {
        return courseRepository.save(Course.builder()
                .title("Java Básico")
                .status("ACTIVE")
                .active(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .instructor(instructor)
                .build());
    }

    private Enrollment savedEnrollment(Student student, Course course, String status) {
        return enrollmentRepository.save(Enrollment.builder()
                .student(student)
                .course(course)
                .status(status)
                .enrolledAt(Instant.now())
                .build());
    }
    // ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("Encuentra matrículas por studentId")
    void shouldFindByStudentId() {
        // Given
        var student = savedStudent();
        var course = savedCourse(savedInstructor());
        savedEnrollment(student, course, "ACTIVE");

        // When
        List<Enrollment> result = enrollmentRepository.findByStudentId(student.getId());

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo("ACTIVE");
    }

    @Test
    @DisplayName("Encuentra matrículas por courseId")
    void shouldFindByCourseId() {
        // Given
        var student = savedStudent();
        var course = savedCourse(savedInstructor());
        savedEnrollment(student, course, "ACTIVE");

        // When
        List<Enrollment> result = enrollmentRepository.findByCourseId(course.getId());

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStudent().getId()).isEqualTo(student.getId());
    }

    @Test
    @DisplayName("Encuentra matrículas por status")
    void shouldFindByStatus() {
        // Given
        var student = savedStudent();
        var course = savedCourse(savedInstructor());
        savedEnrollment(student, course, "ACTIVE");
        savedEnrollment(student, course, "INACTIVE");

        // When
        List<Enrollment> result = enrollmentRepository.findByStatus("ACTIVE");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo("ACTIVE");
    }

    @Test
    @DisplayName("Encuentra matrícula por studentId y courseId")
    void shouldFindByStudentIdAndCourseId() {
        // Given
        var student = savedStudent();
        var course = savedCourse(savedInstructor());
        savedEnrollment(student, course, "ACTIVE");

        // When
        Optional<Enrollment> result = enrollmentRepository
                .findByStudentIdAndCourseId(student.getId(), course.getId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getStatus()).isEqualTo("ACTIVE");
    }

    @Test
    @DisplayName("Retorna vacío cuando no existe matrícula para ese estudiante y curso")
    void shouldReturnEmptyWhenEnrollmentNotFound() {
        // Given — estudiante y curso sin matrícula entre ellos
        var student = savedStudent();
        var course = savedCourse(savedInstructor());

        // When
        Optional<Enrollment> result = enrollmentRepository
                .findByStudentIdAndCourseId(student.getId(), course.getId());

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Encuentra matrículas realizadas después de una fecha")
    void shouldFindByEnrolledAtAfter() {
        // Given
        var student = savedStudent();
        var course = savedCourse(savedInstructor());
        savedEnrollment(student, course, "ACTIVE");

        // When
        List<Enrollment> result = enrollmentRepository
                .findByEnrolledAtAfter(Instant.now().minusSeconds(60));

        // Then
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("Encuentra matrículas por studentId y status con curso cargado")
    void shouldFindByStudentIdAndStatus() {
        // Given
        var student = savedStudent();
        var course = savedCourse(savedInstructor());
        savedEnrollment(student, course, "ACTIVE");
        savedEnrollment(student, course, "INACTIVE");

        // When
        List<Enrollment> result = enrollmentRepository
                .findByStudentIdAndStatus(student.getId(), "ACTIVE");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCourse()).isNotNull(); // verifica el JOIN FETCH
    }

    @Test
    @DisplayName("Cuenta matrículas activas de un curso")
    void shouldCountActiveEnrollmentsByCourse() {
        // Given
        var student = savedStudent();
        var course = savedCourse(savedInstructor());
        savedEnrollment(student, course, "ACTIVE");
        savedEnrollment(student, course, "INACTIVE"); // esta no debe contar

        // When
        Long count = enrollmentRepository.countActiveEnrollmentsByCourse(course.getId());

        // Then
        assertThat(count).isEqualTo(1L);
    }

    @Test
    @DisplayName("Retorna los estudiantes matriculados en un curso")
    void shouldFindStudentsByCourseId() {
        // Given
        var student = savedStudent();
        var course = savedCourse(savedInstructor());
        savedEnrollment(student, course, "ACTIVE");

        // When
        List<Object> result = enrollmentRepository.findStudentsByCourseId(course.getId());

        // Then
        assertThat(result).hasSize(1);
    }
}
