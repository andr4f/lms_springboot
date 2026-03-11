package edu.unimagdalena.lms.repositories;

import edu.unimagdalena.lms.entities.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StudentRepository extends JpaRepository<Student, UUID> {

    Optional<Student> findByEmail(String email);
    Optional<Student> findByFullName(String fullName);
    List<Student> findByFullNameContainingIgnoreCase(String name);

    @Query("SELECT s FROM Student s JOIN s.enrollments e WHERE e.course.id = :courseId")
    List<Student> findStudentsByCourseId(@Param("courseId") UUID courseId);

    @Query("SELECT DISTINCT s FROM Student s JOIN s.assessments a WHERE a.score >= :minScore")
    List<Student> findStudentsWithPassingScore(@Param("minScore") int minScore);

    @Query("SELECT s FROM Student s LEFT JOIN FETCH s.enrollments WHERE s.id = :id")
    Optional<Student> findByIdWithEnrollments(@Param("id") UUID id);
}
