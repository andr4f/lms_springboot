package edu.unimagdalena.lms.repositories;

import edu.unimagdalena.lms.entities.Course;
import edu.unimagdalena.lms.entities.Instructor;
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
class CourseRepositoryTest extends AbstractRepositoryTest {



    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private InstructorRepository instructorRepository;

    @BeforeEach
    void setUp() {
        courseRepository.deleteAll();
        instructorRepository.deleteAll();
    }

    private Instructor buildInstructor(String email, String fullName) {
        Instructor i = new Instructor();
        i.setEmail(email);
        i.setFullName(fullName);
        i.setCreatedAt(Instant.now());
        i.setUpdatedAt(Instant.now());
        return instructorRepository.save(i);
    }

    private Course buildCourse(String title, String status, boolean active, Instructor instructor) {
        Course c = new Course();
        c.setTitle(title);
        c.setStatus(status);
        c.setActive(active);
        c.setInstructor(instructor);
        c.setCreatedAt(Instant.now());
        c.setUpdatedAt(Instant.now());
        return c;
    }

    @Test
    void debeGuardarCurso() {
        Instructor instructor = buildInstructor("inst@test.com", "Profesor Test");
        Course saved = courseRepository.save(
                buildCourse("Curso Spring", "PUBLISHED", true, instructor)
        );

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getTitle()).isEqualTo("Curso Spring");
    }

    @Test
    void debeBuscarPorTitulo() {
        Instructor instructor = buildInstructor("find@test.com", "Buscar");
        courseRepository.save(buildCourse("Java Básico", "DRAFT", true, instructor));

        Optional<Course> resultado = courseRepository.findByTitle("Java Básico");

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getStatus()).isEqualTo("DRAFT");
    }

    @Test
    void debeListarSoloActivos() {
        Instructor instructor = buildInstructor("act@test.com", "Activo");
        courseRepository.save(buildCourse("Activo 1", "PUBLISHED", true, instructor));
        courseRepository.save(buildCourse("Inactivo 1", "PUBLISHED", false, instructor));

        List<Course> activos = courseRepository.findByActiveTrue();

        assertThat(activos).hasSize(1);
        assertThat(activos.get(0).getActive()).isTrue();
    }

    @Test
    void debeBuscarActivosPorInstructor() {
        Instructor instructor = buildInstructor("inst2@test.com", "Otro Profesor");
        Instructor instructor2 = buildInstructor("inst3@test.com", "Tercero");

        courseRepository.save(buildCourse("C1", "PUBLISHED", true, instructor));
        courseRepository.save(buildCourse("C2", "PUBLISHED", false, instructor));
        courseRepository.save(buildCourse("C3", "PUBLISHED", true, instructor2));

        List<Course> cursos = courseRepository.findActiveCoursesByInstructor(instructor.getId());

        assertThat(cursos).hasSize(1);
        assertThat(cursos.get(0).getInstructor().getId()).isEqualTo(instructor.getId());
    }

    @Test
    void debeBuscarPorTituloConteniendo() {
        Instructor instructor = buildInstructor("search@test.com", "Search");
        courseRepository.save(buildCourse("Java Avanzado", "PUBLISHED", true, instructor));
        courseRepository.save(buildCourse("Spring Boot", "PUBLISHED", true, instructor));

        List<Course> cursos = courseRepository.findByTitleContainingIgnoreCase("java");

        assertThat(cursos).hasSize(1);
        assertThat(cursos.get(0).getTitle()).isEqualTo("Java Avanzado");
    }
}
