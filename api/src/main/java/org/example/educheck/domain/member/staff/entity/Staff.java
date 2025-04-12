package org.example.educheck.domain.member.staff.entity;

import jakarta.persistence.*;
import lombok.Getter;
import org.example.educheck.domain.member.entity.Member;
import org.example.educheck.domain.staffcourse.repository.StaffCourseRepository;

@Entity
@Getter
public class Staff {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "member_id")
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "VARCHAR(100)")
    private Type type;

    public boolean hasAccessToCourse(Long courseId, StaffCourseRepository repository) {
        return repository.existsByStaffIdAndCourseId(this.id, courseId);
    }

}
