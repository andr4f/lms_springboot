package edu.unimagdalena.lms.services;

import edu.unimagdalena.lms.entities.Student;
import edu.unimagdalena.lms.repositories.StudentRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class StudentService {

    private final StudentRepository studentRepository;

    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    public Student crear(Student student) {
        // Regla de negocio: email único
        studentRepository.findByEmail(student.getEmail())
                .ifPresent(s -> {
                    throw new RuntimeException("El email ya está registrado");
                });

        student.setCreatedAt(Instant.now());
        student.setUpdatedAt(Instant.now());
        return studentRepository.save(student);
    }

    public Student buscarPorId(UUID id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Estudiante no encontrado"));
    }

    public List<Student> listarTodos() {
        return studentRepository.findAll();
    }

    public Student actualizar(UUID id, String nuevoNombre, String nuevoEmail) {
        Student existente = buscarPorId(id);

        if (nuevoNombre != null && !nuevoNombre.isBlank()) {
            existente.setFullName(nuevoNombre);
        }

        if (nuevoEmail != null && !nuevoEmail.isBlank()
                && !nuevoEmail.equalsIgnoreCase(existente.getEmail())) {

            studentRepository.findByEmail(nuevoEmail)
                    .ifPresent(s -> {
                        throw new RuntimeException("El email ya está registrado");
                    });

            existente.setEmail(nuevoEmail);
        }

        existente.setUpdatedAt(Instant.now());
        return studentRepository.save(existente);
    }

    public void eliminar(UUID id) {
        if (!studentRepository.existsById(id)) {
            throw new RuntimeException("Estudiante no encontrado");
        }
        studentRepository.deleteById(id);
    }
}
