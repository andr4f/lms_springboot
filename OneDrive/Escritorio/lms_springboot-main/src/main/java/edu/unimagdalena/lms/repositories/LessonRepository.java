package edu.unimagdalena.lms.repositories;

import edu.unimagdalena.lms.entities.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LessonRepository extends JpaRepository<Lesson, UUID> {

    List<Lesson> findByCourseId(UUID courseId);
    List<Lesson> findByCourseIdOrderByOrderIndexAsc(UUID courseId);
    Optional<Lesson> findByTitle(String title);
    List<Lesson> findByCourseIdAndOrderIndexGreaterThan(UUID courseId, int index);

    @Query("SELECT l FROM Lesson l JOIN FETCH l.course WHERE l.course.id = :courseId ORDER BY l.orderIndex ASC")
    List<Lesson> findLessonsWithCourse(@Param("courseId") UUID courseId);

    @Query("SELECT COUNT(l) FROM Lesson l WHERE l.course.id = :courseId")
    Long countLessonsByCourse(@Param("courseId") UUID courseId);
}
