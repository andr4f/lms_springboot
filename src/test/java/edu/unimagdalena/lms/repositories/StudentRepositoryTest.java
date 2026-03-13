package edu.unimagdalena.lms.repositories;

import edu.unimagdalena.lms.entities.Student;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.*;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers


@org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase(replace = org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace.NONE)

class StudentRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private StudentRepository studentRepository;

    @BeforeEach
    void setUp() {
        studentRepository.deleteAll();
    }

    private Student buildStudent(String email, String fullName) {
        Student s = new Student();
        s.setEmail(email);
        s.setFullName(fullName);
        s.setCreatedAt(Instant.now());
        s.setUpdatedAt(Instant.now());
        return s;
    }

    // CREATE
    @Test
    void debeGuardarNuevoEstudiante() {
        Student saved = studentRepository.save(buildStudent("carlos@test.com", "Carlos Pérez"));
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getEmail()).isEqualTo("carlos@test.com");
    }

    // READ - findById
    @Test
    void debeEncontrarPorId() {
        Student saved = studentRepository.save(buildStudent("find@test.com", "Para Buscar"));
        Optional<Student> resultado = studentRepository.findById(saved.getId());
        assertThat(resultado).isPresent();
        assertThat(resultado.get().getFullName()).isEqualTo("Para Buscar");
    }

    // READ - findAll
    @Test
    void debeListarTodos() {
        studentRepository.save(buildStudent("a@test.com", "Estudiante A"));
        studentRepository.save(buildStudent("b@test.com", "Estudiante B"));
        assertThat(studentRepository.findAll()).hasSize(2);
    }

    // READ - findByEmail
    @Test
    void debeBuscarPorEmail() {
        studentRepository.save(buildStudent("email@test.com", "Email Test"));
        Optional<Student> resultado = studentRepository.findByEmail("email@test.com");
        assertThat(resultado).isPresent();
    }

    // UPDATE
    @Test
    void debeActualizarNombre() {
        Student saved = studentRepository.save(buildStudent("update@test.com", "Viejo"));
        saved.setFullName("Nuevo");
        Student updated = studentRepository.save(saved);
        assertThat(updated.getFullName()).isEqualTo("Nuevo");
    }

    // DELETE
    @Test
    void debeEliminarPorId() {
        Student saved = studentRepository.save(buildStudent("delete@test.com", "Borrar"));
        studentRepository.deleteById(saved.getId());
        assertThat(studentRepository.findById(saved.getId())).isEmpty();
    }
}
