package edu.unimagdalena.lms.repositories;

import edu.unimagdalena.lms.entities.Instructor;
import edu.unimagdalena.lms.entities.Course;
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

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class InstructorRepositoryTest extends AbstractRepositoryTest {


    @Autowired
    private InstructorRepository instructorRepository;

    @Autowired
    private CourseRepository courseRepository;

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
        return i;
    }

    @Test
    void debeGuardarInstructor() {
        Instructor saved = instructorRepository.save(buildInstructor("inst@test.com", "Profe Uno"));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getEmail()).isEqualTo("inst@test.com");
    }

    @Test
    void debeBuscarPorEmail() {
        instructorRepository.save(buildInstructor("buscar@test.com", "Buscar"));

        Optional<Instructor> resultado = instructorRepository.findByEmail("buscar@test.com");

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getFullName()).isEqualTo("Buscar");
    }

    @Test
    void debeBuscarPorNombreConteniendo() {
        instructorRepository.save(buildInstructor("a@test.com", "Juan Pérez"));
        instructorRepository.save(buildInstructor("b@test.com", "María López"));

        List<Instructor> resultado = instructorRepository.findByFullNameContainingIgnoreCase("juan");

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getFullName()).containsIgnoringCase("juan");
    }

    @Test
    void debeEncontrarConCursosActivos() {
        Instructor inst = instructorRepository.save(buildInstructor("active@test.com", "Activo"));

        Course c1 = new Course();
        c1.setTitle("Curso 1");
        c1.setInstructor(inst);
        c1.setActive(true);
        c1.setStatus("PUBLISHED");
        c1.setCreatedAt(Instant.now());
        c1.setUpdatedAt(Instant.now());
        courseRepository.save(c1);

        List<Instructor> resultado = instructorRepository.findInstructorsWithActiveCourses();

        assertThat(resultado).extracting(Instructor::getId).contains(inst.getId());
    }

    @Test
    void debeContarCursosPorInstructor() {
        Instructor inst = instructorRepository.save(buildInstructor("count@test.com", "Contar"));

        Course c1 = new Course();
        c1.setTitle("C1");
        c1.setInstructor(inst);
        c1.setActive(true);
        c1.setStatus("PUBLISHED");
        c1.setCreatedAt(Instant.now());
        c1.setUpdatedAt(Instant.now());

        Course c2 = new Course();
        c2.setTitle("C2");
        c2.setInstructor(inst);
        c2.setActive(false);
        c2.setStatus("DRAFT");
        c2.setCreatedAt(Instant.now());
        c2.setUpdatedAt(Instant.now());

        courseRepository.save(c1);
        courseRepository.save(c2);

        Long count = instructorRepository.countCoursesByInstructor(inst.getId());

        assertThat(count).isEqualTo(2L);
    }
}
