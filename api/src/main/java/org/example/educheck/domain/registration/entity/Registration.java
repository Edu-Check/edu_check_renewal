package org.example.educheck.domain.registration.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.educheck.domain.course.entity.Course;
import org.example.educheck.domain.member.student.entity.Student;

import java.time.LocalDate;

@Getter
@Entity
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Registration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course;
    private LocalDate dropDate;
    private LocalDate completionDate;



    @Builder
    public Registration(Student student, Course course) {
        this.student = student;
        this.course = course;
    }
}
