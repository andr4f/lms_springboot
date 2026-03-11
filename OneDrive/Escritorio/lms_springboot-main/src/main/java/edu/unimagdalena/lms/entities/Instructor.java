package edu.unimagdalena.lms.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "instructors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Instructor {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false)
    private String email;
    @Column(name = "full_name",nullable = false)
    private String fullName;
    @Column(name = "created_at")
    private Instant createdAt;
    @Column(name = "updated_at")
    private Instant updatedAt;
    
    @OneToOne(mappedBy = "instructor", cascade = CascadeType.ALL)
    private InstructorProfile profile;
    @OneToMany(mappedBy = "instructor", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Course> courses = new ArrayList<>();

    public void addCourse(Course course) {
        courses.add(course);
    }
}
