package org.example.educheck.domain.member.student.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.educheck.domain.member.entity.Member;
import org.example.educheck.domain.registration.entity.Registration;
import org.example.educheck.domain.registration.repository.RegistrationRepository;
import org.example.educheck.global.common.exception.custom.common.ResourceMismatchException;

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


    private char courseParticipationStatus;

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Registration> registrations = new ArrayList<>();

    @Builder
    public Student(Member member, char courseParticipationStatus, List<Registration> registrations) {
        this.member = member;
        this.courseParticipationStatus = courseParticipationStatus;
        if (registrations != null) {
            this.registrations = registrations;
        }
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

    public void validateEnrolledInCourse(Long courseId, RegistrationRepository registrationRepository) {
        boolean isExist = registrationRepository.existsByStudentIdAndCourseId(this.id, courseId);
        if (!isExist) {
            throw new ResourceMismatchException("해당 과정에 등록된 학생이 아닙니다.");
        }
    }
}
