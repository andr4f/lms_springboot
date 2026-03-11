package edu.unimagdalena.lms.repositories;

import edu.unimagdalena.lms.entities.Instructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InstructorRepository extends JpaRepository<Instructor, UUID> {

    Optional<Instructor> findByEmail(String email);
    Optional<Instructor> findByFullName(String fullName);
    List<Instructor> findByFullNameContainingIgnoreCase(String name);

    @Query("SELECT DISTINCT i FROM Instructor i JOIN i.courses c WHERE c.active = true")
    List<Instructor> findInstructorsWithActiveCourses();

    @Query("SELECT i FROM Instructor i LEFT JOIN FETCH i.profile WHERE i.id = :id")
    Optional<Instructor> findByIdWithProfile(@Param("id") UUID id);

    @Query("SELECT COUNT(c) FROM Course c WHERE c.instructor.id = :instructorId")
    Long countCoursesByInstructor(@Param("instructorId") UUID instructorId);
}
