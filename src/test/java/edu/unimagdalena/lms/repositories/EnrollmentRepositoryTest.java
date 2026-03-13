package edu.unimagdalena.lms.repositories;

import edu.unimagdalena.lms.entities.Course;
import edu.unimagdalena.lms.entities.Enrollment;
import edu.unimagdalena.lms.entities.Instructor;
import edu.unimagdalena.lms.entities.Student;
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
class EnrollmentRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private InstructorRepository instructorRepository;

    @BeforeEach
    void setUp() {
        enrollmentRepository.deleteAll();
        courseRepository.deleteAll();
        studentRepository.deleteAll();
        instructorRepository.deleteAll();
    }

    private Student buildStudent(String email, String fullName) {
        Student s = new Student();
        s.setEmail(email);
        s.setFullName(fullName);
        s.setCreatedAt(Instant.now());
        s.setUpdatedAt(Instant.now());
        return studentRepository.save(s);
    }

    private Course buildCourse(String title) {
        Instructor inst = new Instructor();
        inst.setEmail("inst@test.com");
        inst.setFullName("Profe Enroll");
        inst.setCreatedAt(Instant.now());
        inst.setUpdatedAt(Instant.now());
        inst = instructorRepository.save(inst);

        Course c = new Course();
        c.setTitle(title);
        c.setInstructor(inst);
        c.setActive(true);
        c.setStatus("PUBLISHED");
        c.setCreatedAt(Instant.now());
        c.setUpdatedAt(Instant.now());
        return courseRepository.save(c);
    }

    private Enrollment buildEnrollment(Student student, Course course, String status) {
        Enrollment e = new Enrollment();
        e.setStudent(student);
        e.setCourse(course);
        e.setStatus(status);
        e.setEnrolledAt(Instant.now());
        return e;
    }

    @Test
    void debeGuardarInscripcion() {
        Student s = buildStudent("stu@test.com", "Alumno");
        Course c = buildCourse("Curso");
        Enrollment saved = enrollmentRepository.save(buildEnrollment(s, c, "ACTIVE"));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getStudent().getId()).isEqualTo(s.getId());
    }

    @Test
    void debeBuscarPorStudentIdYCourseId() {
        Student s = buildStudent("combo@test.com", "Combo");
        Course c = buildCourse("Curso Combo");
        enrollmentRepository.save(buildEnrollment(s, c, "ACTIVE"));

        Optional<Enrollment> resultado =
                enrollmentRepository.findByStudentIdAndCourseId(s.getId(), c.getId());

        assertThat(resultado).isPresent();
    }

    @Test
    void debeListarPorStudentIdYStatus() {
        Student s = buildStudent("status@test.com", "Status");
        Course c1 = buildCourse("C1");
        Course c2 = buildCourse("C2");

        enrollmentRepository.save(buildEnrollment(s, c1, "ACTIVE"));
        enrollmentRepository.save(buildEnrollment(s, c2, "CANCELLED"));

        List<Enrollment> activos =
                enrollmentRepository.findByStudentIdAndStatus(s.getId(), "ACTIVE");

        assertThat(activos).hasSize(1);
        assertThat(activos.get(0).getCourse().getTitle()).isEqualTo("C1");
    }

    @Test
    void debeContarActivosPorCurso() {
        Student s1 = buildStudent("a@test.com", "A");
        Student s2 = buildStudent("b@test.com", "B");
        Course c = buildCourse("Curso Conteo");

        enrollmentRepository.save(buildEnrollment(s1, c, "ACTIVE"));
        enrollmentRepository.save(buildEnrollment(s2, c, "ACTIVE"));
        enrollmentRepository.save(buildEnrollment(buildStudent("c@test.com", "C"), c, "CANCELLED"));

        Long count = enrollmentRepository.countActiveEnrollmentsByCourse(c.getId());

        assertThat(count).isEqualTo(2L);
    }

    @Test
    void debeListarEstudiantesPorCurso() {
        Student s1 = buildStudent("x@test.com", "X");
        Student s2 = buildStudent("y@test.com", "Y");
        Course c = buildCourse("Curso Estudiantes");

        enrollmentRepository.save(buildEnrollment(s1, c, "ACTIVE"));
        enrollmentRepository.save(buildEnrollment(s2, c, "ACTIVE"));

        List<Object> students = enrollmentRepository.findStudentsByCourseId(c.getId());

        assertThat(students).hasSize(2);
    }
}
