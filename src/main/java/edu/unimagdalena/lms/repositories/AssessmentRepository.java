package edu.unimagdalena.lms.repositories;

import edu.unimagdalena.lms.entities.Assessment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface AssessmentRepository extends JpaRepository<Assessment, UUID> {

    List<Assessment> findByStudentId(UUID studentId);
    List<Assessment> findByCourseId(UUID courseId);
    List<Assessment> findByType(String type);
    List<Assessment> findByScoreGreaterThanEqual(Integer score);
    List<Assessment> findByTakenAtBetween(Instant start, Instant end);

    @Query("SELECT AVG(a.score) FROM Assessment a WHERE a.course.id = :courseId")
    Double findAverageScoreByCourseId(@Param("courseId") UUID courseId);

    @Query("SELECT a FROM Assessment a WHERE a.student.id = :studentId AND a.course.id = :courseId")
    List<Assessment> findByStudentIdAndCourseId(@Param("studentId") UUID studentId,
                                                @Param("courseId") UUID courseId);

    @Query("SELECT a FROM Assessment a WHERE a.student.id = :studentId AND a.score >= :minScore ORDER BY a.score DESC")
    List<Assessment> findPassingAssessmentsByStudent(@Param("studentId") UUID studentId,
                                                     @Param("minScore") int minScore);

    @Query("SELECT a.type, MAX(a.score) FROM Assessment a GROUP BY a.type")
    List<Object[]> findMaxScoreByType();
}
