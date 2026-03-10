package edu.unimagdalena.lms.repositories;

import edu.unimagdalena.lms.entities.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CourseRepository extends JpaRepository<Course, UUID> {

    Optional<Course> findByTitle(String title);
    List<Course> findByActiveTrue();
    List<Course> findByStatus(String status);
    List<Course> findByInstructorId(UUID instructorId);
    List<Course> findByCreatedAtAfter(Instant date);

    @Query("SELECT c FROM Course c WHERE c.instructor.id = :instructorId AND c.active = true")
    List<Course> findActiveCoursesByInstructor(@Param("instructorId") UUID instructorId);

    @Query("SELECT c FROM Course c WHERE LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Course> findByTitleContainingIgnoreCase(@Param("keyword") String keyword);

    @Query("SELECT c, COUNT(e) FROM Course c LEFT JOIN c.enrollments e WHERE c.id = :courseId GROUP BY c")
    Object[] countEnrollmentsByCourse(@Param("courseId") UUID courseId);
}
