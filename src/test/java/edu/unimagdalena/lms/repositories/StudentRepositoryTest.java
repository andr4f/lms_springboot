package edu.unimagdalena.lms.repositories;

import edu.unimagdalena.lms.entities.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class StudentRepositoryTest extends AbstractRepositoryIT {

    @Autowired
    StudentRepository studentRepository;
    @Autowired
    InstructorRepository instructorRepository;
    @Autowired
    CourseRepository courseRepository;
    @Autowired
    EnrollmentRepository enrollmentRepository;
    @Autowired
    AssessmentRepository assessmentRepository;

    @Test
    @DisplayName("Encuentra un estudiante por email exacto")
    void shouldFindByEmail() {
        // Given
        studentRepository.save(Student.builder()
                .email("ana@test.com")
                .fullName("Ana García")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build());

        // When
        Optional<Student> result = studentRepository.findByEmail("ana@test.com");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getFullName()).isEqualTo("Ana García");
    }

    @Test
    @DisplayName("Retorna vacío cuando el email no existe")
    void shouldReturnEmptyWhenEmailNotFound() {
        // When
        Optional<Student> result = studentRepository.findByEmail("noexiste@test.com");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Encuentra estudiantes por nombre parcial ignorando mayúsculas")
    void shouldFindByFullNameContainingIgnoreCase() {
        // Given
        studentRepository.save(Student.builder()
                .email("ana@test.com").fullName("Ana García")
                .createdAt(Instant.now()).updatedAt(Instant.now()).build());

        studentRepository.save(Student.builder()
                .email("juan@test.com").fullName("Juan Pérez")
                .createdAt(Instant.now()).updatedAt(Instant.now()).build());

        // When
        List<Student> result = studentRepository
                .findByFullNameContainingIgnoreCase("an");

        // Then
        assertThat(result).hasSize(2); // "Ana" y "Juan" contienen "an"
    }

    @Test
    @DisplayName("Encuentra estudiantes matriculados en un curso")
    void shouldFindStudentsByCourseId() {
        // Given
        var instructor = instructorRepository.save(Instructor.builder()
                .email("prof@test.com").fullName("Prof Test")
                .createdAt(Instant.now()).updatedAt(Instant.now()).build());

        var course = courseRepository.save(Course.builder()
                .title("Java").status("ACTIVE").active(true)
                .createdAt(Instant.now()).updatedAt(Instant.now())
                .instructor(instructor).build());

        var student = studentRepository.save(Student.builder()
                .email("ana@test.com").fullName("Ana García")
                .createdAt(Instant.now()).updatedAt(Instant.now()).build());

        enrollmentRepository.save(Enrollment.builder()
                .student(student).course(course)
                .status("ACTIVE").enrolledAt(Instant.now()).build());

        // When
        List<Student> result = studentRepository.findStudentsByCourseId(course.getId());

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEmail()).isEqualTo("ana@test.com");
    }

    @Test
    @DisplayName("Encuentra estudiantes con puntaje aprobatorio")
    void shouldFindStudentsWithPassingScore() {
        // Given
        var instructor = instructorRepository.save(Instructor.builder()
                .email("prof@test.com").fullName("Prof Test")
                .createdAt(Instant.now()).updatedAt(Instant.now()).build());

        var course = courseRepository.save(Course.builder()
                .title("Java").status("ACTIVE").active(true)
                .createdAt(Instant.now()).updatedAt(Instant.now())
                .instructor(instructor).build());

        var student = studentRepository.save(Student.builder()
                .email("ana@test.com").fullName("Ana García")
                .createdAt(Instant.now()).updatedAt(Instant.now()).build());

        assessmentRepository.save(Assessment.builder()
                .student(student).course(course)
                .score(90).type("EXAM").takenAt(Instant.now()).build());

        // When
        List<Student> result = studentRepository.findStudentsWithPassingScore(80);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEmail()).isEqualTo("ana@test.com");
    }

    @Test
    @DisplayName("Carga enrollments de un estudiante con fetch join")
    void shouldFindByIdWithEnrollments() {
        // Given
        var student = studentRepository.save(Student.builder()
                .email("ana@test.com").fullName("Ana García")
                .createdAt(Instant.now()).updatedAt(Instant.now()).build());

        // When
        Optional<Student> result = studentRepository
                .findByIdWithEnrollments(student.getId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getEnrollments()).isNotNull();
    }
}
