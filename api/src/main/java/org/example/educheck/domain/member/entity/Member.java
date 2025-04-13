package org.example.educheck.domain.member.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.educheck.domain.member.staff.entity.Staff;
import org.example.educheck.domain.member.student.entity.Student;
import org.example.educheck.domain.staffcourse.repository.StaffCourseRepository;
import org.example.educheck.global.common.exception.custom.common.ForbiddenException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Objects;


@Getter
@Setter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;
    private String name;
    private String phoneNumber;
    private LocalDate birthDate;
    private String password;
    private LocalDateTime lastLoginDateTime;
    private LocalDateTime lastPasswordChangeDateTime;
    private LocalDateTime deleteRequestDate;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "VARCHAR(50)")
    private Role role;

    @OneToOne(mappedBy = "member", fetch = FetchType.EAGER)
    private Student student;

    @OneToOne(mappedBy = "member", fetch = FetchType.EAGER)
    private Staff staff;

    @Builder
    public Member(String email, String name, String phoneNumber, LocalDate birthDate, String password, Role role) {
        this.email = email;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.birthDate = birthDate;
        this.password = password;
        this.role = role;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public String getUsername() {
        return email;
    }

    //TODO: 사용하는 로직 보고 수정할 것
    public Long getStudentId() {
        return student != null ? student.getId() : null;
    }


    public boolean isStudent() {
        return role == Role.STUDENT;
    }

    public boolean isMiddleAdmin() {
        return role == Role.MIDDLE_ADMIN;
    }

    public boolean isNotMiddleAdmin() {
        return role != Role.MIDDLE_ADMIN;
    }

    public boolean canAccessCourse(Long courseId, StaffCourseRepository repository) {
        return repository.existsByStaffIdAndCourseId(this.staff.getId(), courseId);
    }

    public void validateStaffAccessToCourse(Long courseId, StaffCourseRepository staffCourseRepository) {
        if (isNotMiddleAdmin()) {
            throw new ForbiddenException("관리자 권한이 없습니다.");
        }

        Optional.ofNullable(this.staff)
                .filter(staff -> staff.hasAccessToCourse(courseId, staffCourseRepository))
                .orElseThrow(() -> new ForbiddenException("해당 과정에 대한 접근 권한이 없습니다."));
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Member member = (Member) o;
        return 
          .equals(id, member.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    public boolean isSameMember(Member member) {
        return Objects.equals(this, member);
    }
}
