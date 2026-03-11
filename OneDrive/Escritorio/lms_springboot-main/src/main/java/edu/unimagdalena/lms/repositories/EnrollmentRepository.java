package edu.unimagdalena.lms.repositories;

import edu.unimagdalena.lms.entities.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EnrollmentRepository extends JpaRepository<Enrollment, UUID> {

    List<Enrollment> findByStudentId(UUID studentId);
    List<Enrollment> findByCourseId(UUID courseId);
    List<Enrollment> findByStatus(String status);
    Optional<Enrollment> findByStudentIdAndCourseId(UUID studentId, UUID courseId);
    List<Enrollment> findByEnrolledAtAfter(Instant date);

    @Query("SELECT e FROM Enrollment e JOIN FETCH e.course WHERE e.student.id = :studentId AND e.status = :status")
    List<Enrollment> findByStudentIdAndStatus(@Param("studentId") UUID studentId,
                                              @Param("status") String status);

    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.course.id = :courseId AND e.status = 'ACTIVE'")
    Long countActiveEnrollmentsByCourse(@Param("courseId") UUID courseId);

    @Query("SELECT e.student FROM Enrollment e WHERE e.course.id = :courseId")
    List<Object> findStudentsByCourseId(@Param("courseId") UUID courseId);
}
