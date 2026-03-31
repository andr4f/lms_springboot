package edu.unimagdalena.lms.services;

import edu.unimagdalena.lms.api.dto.LessonDtos.*;
import edu.unimagdalena.lms.entities.Course;
import edu.unimagdalena.lms.entities.Instructor;
import edu.unimagdalena.lms.entities.Lesson;
import edu.unimagdalena.lms.exception.NotFoundException;
import edu.unimagdalena.lms.repositories.CourseRepository;
import edu.unimagdalena.lms.repositories.LessonRepository;
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
class LessonServiceImplTest {

    @Mock
    LessonRepository lessonRepository;

    @Mock
    CourseRepository courseRepository;

    @InjectMocks
    LessonServiceImpl lessonService;

    // ── helpers ──────────────────────────────────────────
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

    private Lesson buildLesson(UUID id, Course course) {
        return Lesson.builder()
                .id(id)
                .title("Introducción a Java")
                .orderIndex(1)
                .course(course)
                .build();
    }
    // ─────────────────────────────────────────────────────

    @Test
    @DisplayName("Crea una lección cuando el curso existe")
    void shouldCreateLessonWhenCourseExists() {
        var courseId = UUID.randomUUID();
        var course = buildCourse(courseId);
        var req = new LessonRequest("Introducción a Java", 1, courseId);

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(lessonRepository.save(any())).thenAnswer(inv -> {
            Lesson l = inv.getArgument(0);
            l.setId(UUID.randomUUID());
            return l;
        });

        LessonResponse result = lessonService.create(req);

        assertThat(result).isNotNull();
        assertThat(result.title()).isEqualTo("Introducción a Java");
        assertThat(result.orderIndex()).isEqualTo(1);
        assertThat(result.courseId()).isEqualTo(courseId);
        verify(courseRepository).findById(courseId);
        verify(lessonRepository).save(any(Lesson.class));
    }

    @Test
    @DisplayName("Lanza NotFoundException al crear lección con curso inexistente")
    void shouldThrowNotFoundWhenCourseNotExists() {
        var courseId = UUID.randomUUID();
        var req = new LessonRequest("Intro", 1, courseId);

        when(courseRepository.findById(courseId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> lessonService.create(req))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining(courseId.toString());
        verify(lessonRepository, never()).save(any());
    }

    @Test
    @DisplayName("Retorna la lección cuando existe")
    void shouldReturnLessonWhenFound() {
        var id = UUID.randomUUID();
        var course = buildCourse(UUID.randomUUID());

        when(lessonRepository.findById(id))
                .thenReturn(Optional.of(buildLesson(id, course)));

        LessonResponse result = lessonService.get(id);

        assertThat(result.id()).isEqualTo(id);
        assertThat(result.title()).isEqualTo("Introducción a Java");
    }

    @Test
    @DisplayName("Lanza NotFoundException cuando la lección no existe")
    void shouldThrowNotFoundWhenLessonNotFound() {
        var id = UUID.randomUUID();
        when(lessonRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> lessonService.get(id))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining(id.toString());
    }

    @Test
    @DisplayName("Retorna lista de todas las lecciones")
    void shouldReturnAllLessons() {
        var course = buildCourse(UUID.randomUUID());
        when(lessonRepository.findAll()).thenReturn(List.of(
                buildLesson(UUID.randomUUID(), course),
                buildLesson(UUID.randomUUID(), course),
                buildLesson(UUID.randomUUID(), course)
        ));

        List<LessonResponse> result = lessonService.list();

        assertThat(result).hasSize(3);
    }

    @Test
    @DisplayName("Retorna lecciones filtradas por curso")
    void shouldReturnLessonsByCourse() {
        var courseId = UUID.randomUUID();
        var course = buildCourse(courseId);
        when(lessonRepository.findByCourseId(courseId)).thenReturn(List.of(
                buildLesson(UUID.randomUUID(), course),
                buildLesson(UUID.randomUUID(), course)
        ));

        List<LessonResponse> result = lessonService.listByCourse(courseId);

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(l -> l.courseId().equals(courseId));
    }

    @Test
    @DisplayName("Actualiza solo el title de la lección")
    void shouldUpdateLessonTitle() {
        var id = UUID.randomUUID();
        var entity = buildLesson(id, buildCourse(UUID.randomUUID()));

        when(lessonRepository.findById(id)).thenReturn(Optional.of(entity));
        when(lessonRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var req = new LessonUpdateRequest("Nuevo Título", null, null); // courseId null = no cambia

        LessonResponse result = lessonService.update(id, req);

        assertThat(result.title()).isEqualTo("Nuevo Título");
        assertThat(result.orderIndex()).isEqualTo(1); // no cambió
        verify(courseRepository, never()).findById(any()); // no fue a buscar curso
}


    @Test
    @DisplayName("Actualiza el orderIndex de la lección")
    void shouldUpdateLessonOrderIndex() {
        var id = UUID.randomUUID();
        var entity = buildLesson(id, buildCourse(UUID.randomUUID()));

        when(lessonRepository.findById(id)).thenReturn(Optional.of(entity));
        when(lessonRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var req = new LessonUpdateRequest(null, 5, null); // courseId null = no cambia

        LessonResponse result = lessonService.update(id, req);

        assertThat(result.title()).isEqualTo("Introducción a Java"); // no cambió
        assertThat(result.orderIndex()).isEqualTo(5);
    }

    @Test
    @DisplayName("Cambia el curso de la lección en el update")
    void shouldUpdateLessonCourse() {
        var id = UUID.randomUUID();
        var oldCourseId = UUID.randomUUID();
        var newCourseId = UUID.randomUUID();
        var entity = buildLesson(id, buildCourse(oldCourseId));
        var newCourse = buildCourse(newCourseId);

        when(lessonRepository.findById(id)).thenReturn(Optional.of(entity));
        when(courseRepository.findById(newCourseId)).thenReturn(Optional.of(newCourse));
        when(lessonRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var req = new LessonUpdateRequest(null, null, newCourseId);

        LessonResponse result = lessonService.update(id, req);

        assertThat(result.courseId()).isEqualTo(newCourseId); // cambió al nuevo curso
        verify(courseRepository).findById(newCourseId);
    }

    @Test
    @DisplayName("Lanza NotFoundException si el nuevo curso no existe en el update")
    void shouldThrowNotFoundWhenNewCourseNotExists() {
        var id = UUID.randomUUID();
        var newCourseId = UUID.randomUUID();
        var entity = buildLesson(id, buildCourse(UUID.randomUUID()));

        when(lessonRepository.findById(id)).thenReturn(Optional.of(entity));
        when(courseRepository.findById(newCourseId)).thenReturn(Optional.empty());

        var req = new LessonUpdateRequest(null, null, newCourseId);

        assertThatThrownBy(() -> lessonService.update(id, req))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining(newCourseId.toString());
        verify(lessonRepository, never()).save(any());
}

    @Test
    @DisplayName("Lanza NotFoundException al actualizar lección inexistente")
    void shouldThrowNotFoundWhenUpdatingNonExistent() {
        var id = UUID.randomUUID();
        when(lessonRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> lessonService.update(id, new LessonUpdateRequest(null, null, null)))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("Elimina la lección cuando existe")
    void shouldDeleteLessonWhenExists() {
        var id = UUID.randomUUID();
        when(lessonRepository.existsById(id)).thenReturn(true);

        lessonService.delete(id);

        verify(lessonRepository).deleteById(id);
    }

    @Test
    @DisplayName("Lanza NotFoundException al eliminar lección inexistente")
    void shouldThrowNotFoundWhenDeletingNonExistent() {
        var id = UUID.randomUUID();
        when(lessonRepository.existsById(id)).thenReturn(false);

        assertThatThrownBy(() -> lessonService.delete(id))
                .isInstanceOf(NotFoundException.class);
        verify(lessonRepository, never()).deleteById(any());
    }
}