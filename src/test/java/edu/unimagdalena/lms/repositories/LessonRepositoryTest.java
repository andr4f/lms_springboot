package edu.unimagdalena.lms.repositories;

import edu.unimagdalena.lms.entities.Course;
import edu.unimagdalena.lms.entities.Instructor;
import edu.unimagdalena.lms.entities.Lesson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class LessonRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private InstructorRepository instructorRepository;

    @BeforeEach
    void setUp() {
        lessonRepository.deleteAll();
        courseRepository.deleteAll();
        instructorRepository.deleteAll();
    }

    private Course buildCourse() {
        Instructor inst = new Instructor();
        inst.setEmail("inst@test.com");
        inst.setFullName("Profe Lessons");
        inst.setCreatedAt(Instant.now());
        inst.setUpdatedAt(Instant.now());
        inst = instructorRepository.save(inst);

        Course c = new Course();
        c.setTitle("Curso con Lecciones");
        c.setInstructor(inst);
        c.setActive(true);
        c.setStatus("PUBLISHED");
        c.setCreatedAt(Instant.now());
        c.setUpdatedAt(Instant.now());
        return courseRepository.save(c);
    }

    private Lesson buildLesson(Course course, String title, int orderIndex) {
        Lesson l = new Lesson();
        l.setCourse(course);
        l.setTitle(title);
        l.setOrderIndex(orderIndex);
        return l;
    }

    @Test
    void debeGuardarLeccion() {
        Course course = buildCourse();
        Lesson saved = lessonRepository.save(buildLesson(course, "Intro", 1));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCourse().getId()).isEqualTo(course.getId());
    }

    @Test
    void debeListarPorCursoOrdenadas() {
        Course course = buildCourse();
        lessonRepository.save(buildLesson(course, "Tema 2", 2));
        lessonRepository.save(buildLesson(course, "Tema 1", 1));

        List<Lesson> lecciones = lessonRepository.findByCourseIdOrderByOrderIndexAsc(course.getId());

        assertThat(lecciones).hasSize(2);
        assertThat(lecciones.get(0).getOrderIndex()).isEqualTo(1);
    }

    @Test
    void debeBuscarPorTitulo() {
        Course course = buildCourse();
        lessonRepository.save(buildLesson(course, "Mi Título", 1));

        Optional<Lesson> resultado = lessonRepository.findByTitle("Mi Título");

        assertThat(resultado).isPresent();
    }

    @Test
    void debeListarMayoresAIndice() {
        Course course = buildCourse();
        lessonRepository.save(buildLesson(course, "L1", 1));
        lessonRepository.save(buildLesson(course, "L2", 2));
        lessonRepository.save(buildLesson(course, "L3", 3));

        List<Lesson> posteriores = lessonRepository.findByCourseIdAndOrderIndexGreaterThan(course.getId(), 1);

        assertThat(posteriores).hasSize(2);
    }

    @Test
    void debeContarLeccionesPorCurso() {
        Course course = buildCourse();
        lessonRepository.save(buildLesson(course, "L1", 1));
        lessonRepository.save(buildLesson(course, "L2", 2));

        Long count = lessonRepository.countLessonsByCourse(course.getId());

        assertThat(count).isEqualTo(2L);
    }
}
