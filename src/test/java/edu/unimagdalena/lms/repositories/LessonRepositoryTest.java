package edu.unimagdalena.lms.repositories;

import edu.unimagdalena.lms.entities.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class LessonRepositoryTest extends AbstractRepositoryIT {

    @Autowired LessonRepository lessonRepository;
    @Autowired CourseRepository courseRepository;
    @Autowired InstructorRepository instructorRepository;

    // ── helpers ──────────────────────────────────────────────
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

    private Lesson savedLesson(Course course, String title, int orderIndex) {
        return lessonRepository.save(Lesson.builder()
                .title(title)
                .orderIndex(orderIndex)
                .course(course)
                .build());
    }
    // ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("Encuentra lecciones por courseId")
    void shouldFindByCourseId() {
        // Given
        var course = savedCourse(savedInstructor());
        savedLesson(course, "Intro a Java", 1);
        savedLesson(course, "Variables", 2);

        // When
        List<Lesson> result = lessonRepository.findByCourseId(course.getId());

        // Then
        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("Encuentra lecciones ordenadas por orderIndex ascendente")
    void shouldFindByCourseIdOrderByOrderIndexAsc() {
        // Given
        var course = savedCourse(savedInstructor());
        savedLesson(course, "Lección 3", 3);
        savedLesson(course, "Lección 1", 1);
        savedLesson(course, "Lección 2", 2);

        // When
        List<Lesson> result = lessonRepository.findByCourseIdOrderByOrderIndexAsc(course.getId());

        // Then
        assertThat(result).hasSize(3);
        assertThat(result.get(0).getOrderIndex()).isEqualTo(1);
        assertThat(result.get(1).getOrderIndex()).isEqualTo(2);
        assertThat(result.get(2).getOrderIndex()).isEqualTo(3);
    }

    @Test
    @DisplayName("Encuentra una lección por título exacto")
    void shouldFindByTitle() {
        // Given
        var course = savedCourse(savedInstructor());
        savedLesson(course, "Intro a Java", 1);

        // When
        Optional<Lesson> result = lessonRepository.findByTitle("Intro a Java");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getTitle()).isEqualTo("Intro a Java");
    }

    @Test
    @DisplayName("Retorna vacío cuando el título no existe")
    void shouldReturnEmptyWhenTitleNotFound() {
        // When
        Optional<Lesson> result = lessonRepository.findByTitle("No existe");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Encuentra lecciones con orderIndex mayor a un valor dado")
    void shouldFindByCourseIdAndOrderIndexGreaterThan() {
        // Given
        var course = savedCourse(savedInstructor());
        savedLesson(course, "Lección 1", 1);
        savedLesson(course, "Lección 2", 2);
        savedLesson(course, "Lección 3", 3);

        // When
        List<Lesson> result = lessonRepository
                .findByCourseIdAndOrderIndexGreaterThan(course.getId(), 1);

        // Then
        assertThat(result).hasSize(2); // solo lección 2 y 3
        assertThat(result).allMatch(l -> l.getOrderIndex() > 1);
    }

    @Test
    @DisplayName("Carga lecciones con su curso usando JOIN FETCH")
    void shouldFindLessonsWithCourse() {
        // Given
        var course = savedCourse(savedInstructor());
        savedLesson(course, "Intro a Java", 1);
        savedLesson(course, "Variables", 2);

        // When
        List<Lesson> result = lessonRepository.findLessonsWithCourse(course.getId());

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getCourse()).isNotNull();
        assertThat(result.get(0).getCourse().getId()).isEqualTo(course.getId());
        assertThat(result.get(0).getOrderIndex()).isEqualTo(1); // verifica el orden
    }

    @Test
    @DisplayName("Cuenta el total de lecciones de un curso")
    void shouldCountLessonsByCourse() {
        // Given
        var course = savedCourse(savedInstructor());
        savedLesson(course, "Lección 1", 1);
        savedLesson(course, "Lección 2", 2);
        savedLesson(course, "Lección 3", 3);

        // When
        Long count = lessonRepository.countLessonsByCourse(course.getId());

        // Then
        assertThat(count).isEqualTo(3L);
    }
}
