package edu.unimagdalena.lms.repositories;

import edu.unimagdalena.lms.entities.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AssessmentRepositoryTest extends AbstractRepositoryIT {

    @Autowired AssessmentRepository assessmentRepository;
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

    private Assessment savedAssessment(Student student, Course course,
                                        int score, String type) {
        return assessmentRepository.save(Assessment.builder()
                .student(student)
                .course(course)
                .score(score)
                .type(type)
                .takenAt(Instant.now())
                .build());
    }
    // ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("Encuentra evaluaciones por studentId")
    void shouldFindByStudentId() {
        // Given
        var student = savedStudent();
        var course = savedCourse(savedInstructor());
        savedAssessment(student, course, 80, "QUIZ");
        savedAssessment(student, course, 90, "EXAM");

        // When
        List<Assessment> result = assessmentRepository.findByStudentId(student.getId());

        // Then
        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("Encuentra evaluaciones por courseId")
    void shouldFindByCourseId() {
        // Given
        var student = savedStudent();
        var course = savedCourse(savedInstructor());
        savedAssessment(student, course, 85, "QUIZ");

        // When
        List<Assessment> result = assessmentRepository.findByCourseId(course.getId());

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getScore()).isEqualTo(85);
    }

    @Test
    @DisplayName("Encuentra evaluaciones por tipo")
    void shouldFindByType() {
        // Given
        var student = savedStudent();
        var course = savedCourse(savedInstructor());
        savedAssessment(student, course, 80, "QUIZ");
        savedAssessment(student, course, 90, "EXAM");

        // When
        List<Assessment> result = assessmentRepository.findByType("QUIZ");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getType()).isEqualTo("QUIZ");
    }

    @Test
    @DisplayName("Encuentra evaluaciones con puntaje mayor o igual al dado")
    void shouldFindByScoreGreaterThanEqual() {
        // Given
        var student = savedStudent();
        var course = savedCourse(savedInstructor());
        savedAssessment(student, course, 60, "QUIZ");  // no debe aparecer
        savedAssessment(student, course, 80, "EXAM");  // sí debe aparecer
        savedAssessment(student, course, 95, "QUIZ");  // sí debe aparecer

        // When
        List<Assessment> result = assessmentRepository.findByScoreGreaterThanEqual(80);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(a -> a.getScore() >= 80);
    }

    @Test
    @DisplayName("Encuentra evaluaciones entre dos fechas")
    void shouldFindByTakenAtBetween() {
        // Given
        var student = savedStudent();
        var course = savedCourse(savedInstructor());
        savedAssessment(student, course, 75, "QUIZ");

        Instant start = Instant.now().minusSeconds(60);
        Instant end   = Instant.now().plusSeconds(60);

        // When
        List<Assessment> result = assessmentRepository.findByTakenAtBetween(start, end);

        // Then
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("Calcula el promedio de puntajes de un curso")
    void shouldFindAverageScoreByCourseId() {
        // Given
        var student = savedStudent();
        var course = savedCourse(savedInstructor());
        savedAssessment(student, course, 80, "QUIZ");
        savedAssessment(student, course, 100, "EXAM");

        // When
        Double avg = assessmentRepository.findAverageScoreByCourseId(course.getId());

        // Then
        assertThat(avg).isEqualTo(90.0);
    }

    @Test
    @DisplayName("Encuentra evaluaciones por studentId y courseId")
    void shouldFindByStudentIdAndCourseId() {
        // Given
        var student = savedStudent();
        var course = savedCourse(savedInstructor());
        savedAssessment(student, course, 85, "QUIZ");

        // When
        List<Assessment> result = assessmentRepository
                .findByStudentIdAndCourseId(student.getId(), course.getId());

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getScore()).isEqualTo(85);
    }

    @Test
    @DisplayName("Encuentra evaluaciones aprobatorias de un estudiante ordenadas por puntaje")
    void shouldFindPassingAssessmentsByStudent() {
        // Given
        var student = savedStudent();
        var course = savedCourse(savedInstructor());
        savedAssessment(student, course, 60, "QUIZ");  // no aprueba
        savedAssessment(student, course, 85, "EXAM");  // aprueba
        savedAssessment(student, course, 95, "QUIZ");  // aprueba

        // When
        List<Assessment> result = assessmentRepository
                .findPassingAssessmentsByStudent(student.getId(), 80);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getScore()).isEqualTo(95); // ordenado DESC
        assertThat(result.get(1).getScore()).isEqualTo(85);
    }

    @Test
    @DisplayName("Retorna el puntaje máximo agrupado por tipo")
    void shouldFindMaxScoreByType() {
        // Given
        var student = savedStudent();
        var course = savedCourse(savedInstructor());
        savedAssessment(student, course, 70, "QUIZ");
        savedAssessment(student, course, 90, "QUIZ");  // max QUIZ = 90
        savedAssessment(student, course, 85, "EXAM");  // max EXAM = 85

        // When
        List<Object[]> result = assessmentRepository.findMaxScoreByType();

        // Then
        assertThat(result).hasSize(2); // un resultado por cada tipo
        // verifica que los tipos y máximos son correctos
        result.forEach(row -> {
            String type  = (String) row[0];
            Integer max  = (Integer) row[1];
            if (type.equals("QUIZ")) assertThat(max).isEqualTo(90);
            if (type.equals("EXAM")) assertThat(max).isEqualTo(85);
        });
    }
}
