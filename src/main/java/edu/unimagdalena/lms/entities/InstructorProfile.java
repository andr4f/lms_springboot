package edu.unimagdalena.lms.entities;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "instructor_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InstructorProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false)
    private String phone;
    @Column()
    private String bio;
    @OneToOne
    @JoinColumn(name = "instructor_id", unique = true, nullable = false)
    private Instructor instructor;
}

