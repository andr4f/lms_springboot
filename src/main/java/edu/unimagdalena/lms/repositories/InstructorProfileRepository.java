package edu.unimagdalena.lms.repositories;

import edu.unimagdalena.lms.entities.InstructorProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface InstructorProfileRepository extends JpaRepository<InstructorProfile, UUID> {

    Optional<InstructorProfile> findByInstructorId(UUID instructorId);
    Optional<InstructorProfile> findByPhone(String phone);

    @Query("SELECT p FROM InstructorProfile p JOIN FETCH p.instructor WHERE p.instructor.id = :instructorId")
    Optional<InstructorProfile> findProfileWithInstructor(@Param("instructorId") UUID instructorId);
}
