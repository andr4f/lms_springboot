package edu.unimagdalena.lms.repositories;

import edu.unimagdalena.lms.entities.Assessment;
import edu.unimagdalena.lms.entities.Course;
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
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AssessmentRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("lms_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private AssessmentRepository assessmentRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private InstructorRepository instructorRepository;

    @BeforeEach
    void setUp() {
        assessmentRepository.deleteAll();
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
        inst.setEmail("inst" + System.nanoTime() + "@test.com");
        inst.setFullName("Profe Assessment");
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

    private Assessment buildAssessment(Student student, Course course,
                                       String type, int score, Instant takenAt) {
        Assessment a = new Assessment();
        a.setStudent(student);
        a.setCourse(course);
        a.setType(type);
        a.setScore(score);
        a.setTakenAt(takenAt);
        return a;
    }

    // CREATE
    @Test
    void debeGuardarAssessment() {
        Student s = buildStudent("save@test.com", "Guardar");
        Course c = buildCourse("Curso Save");

        Assessment saved = assessmentRepository.save(
                buildAssessment(s, c, "QUIZ", 85, Instant.now())
        );

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getScore()).isEqualTo(85);
    }

    // findByStudentId
    @Test
    void debeBuscarPorStudentId() {
        Student s = buildStudent("stud@test.com", "Estudiante");
        Course c = buildCourse("Curso S");

        assessmentRepository.save(buildAssessment(s, c, "QUIZ", 70, Instant.now()));
        assessmentRepository.save(buildAssessment(s, c, "EXAM", 90, Instant.now()));

        List<Assessment> resultado = assessmentRepository.findByStudentId(s.getId());

        assertThat(resultado).hasSize(2);
        assertThat(resultado).allMatch(a -> a.getStudent().getId().equals(s.getId()));
    }

    // findByCourseId
    @Test
    void debeBuscarPorCourseId() {
        Student s1 = buildStudent("c1@test.com", "Uno");
        Student s2 = buildStudent("c2@test.com", "Dos");
        Course c = buildCourse("Curso ID");

        assessmentRepository.save(buildAssessment(s1, c, "QUIZ", 60, Instant.now()));
        assessmentRepository.save(buildAssessment(s2, c, "QUIZ", 75, Instant.now()));

        List<Assessment> resultado = assessmentRepository.findByCourseId(c.getId());

        assertThat(resultado).hasSize(2);
    }

    // findByType
    @Test
    void debeBuscarPorTipo() {
        Student s = buildStudent("type@test.com", "Tipo");
        Course c = buildCourse("Curso Tipo");

        assessmentRepository.save(buildAssessment(s, c, "QUIZ", 80, Instant.now()));
        assessmentRepository.save(buildAssessment(s, c, "EXAM", 90, Instant.now()));

        List<Assessment> quizzes = assessmentRepository.findByType("QUIZ");

        assertThat(quizzes).hasSize(1);
        assertThat(quizzes.get(0).getType()).isEqualTo("QUIZ");
    }

    // findByScoreGreaterThanEqual
    @Test
    void debeBuscarPorScoreMayorOIgual() {
        Student s = buildStudent("score@test.com", "Score");
        Course c = buildCourse("Curso Score");

        assessmentRepository.save(buildAssessment(s, c, "QUIZ", 50, Instant.now()));
        assessmentRepository.save(buildAssessment(s, c, "QUIZ", 70, Instant.now()));
        assessmentRepository.save(buildAssessment(s, c, "QUIZ", 90, Instant.now()));

        List<Assessment> resultado = assessmentRepository.findByScoreGreaterThanEqual(70);

        assertThat(resultado).hasSize(2);
        assertThat(resultado).allMatch(a -> a.getScore() >= 70);
    }

    // findByTakenAtBetween
    @Test
    void debeBuscarPorRangoDeFecha() {
        Student s = buildStudent("date@test.com", "Fecha");
        Course c = buildCourse("Curso Fecha");

        Instant ahora = Instant.now();
        Instant hace5Dias = ahora.minus(5, ChronoUnit.DAYS);
        Instant hace2Dias = ahora.minus(2, ChronoUnit.DAYS);
        Instant hace10Dias = ahora.minus(10, ChronoUnit.DAYS);

        assessmentRepository.save(buildAssessment(s, c, "QUIZ", 80, hace2Dias));   // dentro del rango
        assessmentRepository.save(buildAssessment(s, c, "QUIZ", 75, hace10Dias));  // fuera del rango

        Instant inicio = ahora.minus(7, ChronoUnit.DAYS);
        Instant fin    = ahora.minus(1, ChronoUnit.DAYS);

        List<Assessment> resultado = assessmentRepository.findByTakenAtBetween(inicio, fin);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getScore()).isEqualTo(80);
    }

    // findAverageScoreByCourseId
    @Test
    void debeCalcularPromedioPorCurso() {
        Student s1 = buildStudent("avg1@test.com", "Avg1");
        Student s2 = buildStudent("avg2@test.com", "Avg2");
        Course c = buildCourse("Curso Promedio");

        assessmentRepository.save(buildAssessment(s1, c, "EXAM", 60, Instant.now()));
        assessmentRepository.save(buildAssessment(s2, c, "EXAM", 80, Instant.now()));

        Double promedio = assessmentRepository.findAverageScoreByCourseId(c.getId());

        assertThat(promedio).isEqualTo(70.0);
    }

    // findByStudentIdAndCourseId
    @Test
    void debeBuscarPorStudentYCurso() {
        Student s = buildStudent("sc@test.com", "SC");
        Course c1 = buildCourse("C1");
        Course c2 = buildCourse("C2");

        assessmentRepository.save(buildAssessment(s, c1, "QUIZ", 88, Instant.now()));
        assessmentRepository.save(buildAssessment(s, c2, "QUIZ", 55, Instant.now()));

        List<Assessment> resultado =
                assessmentRepository.findByStudentIdAndCourseId(s.getId(), c1.getId());

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getCourse().getId()).isEqualTo(c1.getId());
    }

    // findPassingAssessmentsByStudent
    @Test
    void debeListarAprobadosPorEstudianteOrdenados() {
        Student s = buildStudent("pass@test.com", "Pasar");
        Course c = buildCourse("Curso Pasar");

        assessmentRepository.save(buildAssessment(s, c, "QUIZ", 95, Instant.now()));
        assessmentRepository.save(buildAssessment(s, c, "QUIZ", 40, Instant.now()));
        assessmentRepository.save(buildAssessment(s, c, "EXAM", 72, Instant.now()));

        List<Assessment> aprobados =
                assessmentRepository.findPassingAssessmentsByStudent(s.getId(), 60);

        assertThat(aprobados).hasSize(2);
        assertThat(aprobados.get(0).getScore()).isGreaterThanOrEqualTo(aprobados.get(1).getScore());
    }

    // findMaxScoreByType
    @Test
    void debeEncontrarMaximoPorTipo() {
        Student s = buildStudent("max@test.com", "Max");
        Course c = buildCourse("Curso Max");

        assessmentRepository.save(buildAssessment(s, c, "QUIZ", 70, Instant.now()));
        assessmentRepository.save(buildAssessment(s, c, "QUIZ", 90, Instant.now()));
        assessmentRepository.save(buildAssessment(s, c, "EXAM", 85, Instant.now()));

        List<Object[]> resultado = assessmentRepository.findMaxScoreByType();

        assertThat(resultado).isNotEmpty();
        assertThat(resultado).anyMatch(r -> r[0].equals("QUIZ") && ((Number) r[1]).intValue() == 90);
        assertThat(resultado).anyMatch(r -> r[0].equals("EXAM") && ((Number) r[1]).intValue() == 85);
    }
}
