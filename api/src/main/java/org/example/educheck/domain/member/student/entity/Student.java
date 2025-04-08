package org.example.educheck.domain.member.student.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.educheck.domain.member.entity.Member;
import org.example.educheck.domain.registration.entity.Registration;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "member_id")
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "VARCHAR(50)")
    private StudentStatus studentStatus; // PENDING, NORMAL, WITHDRAW

    private char courseParticipationStatus;

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Registration> registrations = new ArrayList<>();

    @Builder
    public Student(Member member, StudentStatus studentStatus, char courseParticipationStatus, List<Registration> registrations) {
        this.member = member;
        this.studentStatus = studentStatus;
        this.courseParticipationStatus = courseParticipationStatus;
        this.registrations = registrations;
    }

    public boolean isParticipatingCourse() {
        return this.courseParticipationStatus == 'T';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof Student)) return false;
        Student student = (Student) o;
        return Objects.equals(id, student.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
