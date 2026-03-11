package edu.unimagdalena.lms.repositories;

import edu.unimagdalena.lms.entities.Instructor;
import edu.unimagdalena.lms.entities.InstructorProfile;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class InstructorProfileRepositoryTest {

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
    private InstructorProfileRepository profileRepository;

    @Autowired
    private InstructorRepository instructorRepository;

    @BeforeEach
    void setUp() {
        profileRepository.deleteAll();
        instructorRepository.deleteAll();
    }

    private Instructor buildInstructor() {
        Instructor i = new Instructor();
        i.setEmail("inst@test.com");
        i.setFullName("Profesor Perfil");
        i.setCreatedAt(Instant.now());
        i.setUpdatedAt(Instant.now());
        return instructorRepository.save(i);
    }

    private InstructorProfile buildProfile(Instructor instructor, String phone) {
        InstructorProfile p = new InstructorProfile();
        p.setInstructor(instructor);
        p.setPhone(phone);
        p.setBio("Bio de prueba");
        return p;
    }

    @Test
    void debeGuardarPerfil() {
        Instructor inst = buildInstructor();
        InstructorProfile saved = profileRepository.save(buildProfile(inst, "3001234567"));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getPhone()).isEqualTo("3001234567");
    }

    @Test
    void debeBuscarPorInstructorId() {
        Instructor inst = buildInstructor();
        profileRepository.save(buildProfile(inst, "3000000000"));

        Optional<InstructorProfile> resultado = profileRepository.findByInstructorId(inst.getId());

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getInstructor().getId()).isEqualTo(inst.getId());
    }

    @Test
    void debeBuscarPorTelefono() {
        Instructor inst = buildInstructor();
        profileRepository.save(buildProfile(inst, "3111111111"));

        Optional<InstructorProfile> resultado = profileRepository.findByPhone("3111111111");

        assertThat(resultado).isPresent();
    }

    @Test
    void debeTraerPerfilConInstructor() {
        Instructor inst = buildInstructor();
        profileRepository.save(buildProfile(inst, "3222222222"));

        Optional<InstructorProfile> resultado = profileRepository.findProfileWithInstructor(inst.getId());

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getInstructor().getEmail()).isEqualTo("inst@test.com");
    }
}
