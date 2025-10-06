package org.example.educheck.domain.registration.repository;

import org.example.educheck.domain.course.entity.CourseStatus;
import org.example.educheck.domain.member.student.dto.response.StudentInfoResponseDto;
import org.example.educheck.domain.registration.entity.Registration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RegistrationRepository extends JpaRepository<Registration, Long> {
    @Query("SELECT r FROM Registration r WHERE r.student.id = :studentId AND r.dropDate IS NULL AND r.completionDate IS NULL AND r.course.status != :status")
    Optional<Registration> findActiveRegistrationByStudentId(@Param("studentId") Long studentId, @Param("status") CourseStatus status);


    Optional<Registration> findByStudentIdAndCourseId(Long id, Long courseId);

    boolean existsByStudentIdAndCourseId(Long studentId, Long courseId);

    @Query("""
            SELECT new org.example.educheck.domain.member.student.dto.response.StudentInfoResponseDto(
                        m.id,
                        s.id,
                        m.name,
                        m.email,
                        m.phoneNumber,
                        CASE
                        WHEN r.dropDate IS NOT NULL THEN 'DROPPED'
                        WHEN r.completionDate IS NOT NULL THEN 'COMPLETED'
                        ELSE 'ACTIVE'
                        END)
                        FROM Registration r
                        JOIN r.student s
                        JOIN s.member m
                        WHERE r.course.id = :courseId
            """)
    List<StudentInfoResponseDto> findByCourseIdWithStudentAndMember(@Param("courseId") Long courseId);

    @Query("SELECT r.student.member.id FROM Registration r WHERE r.course.id = :courseId")
    List<Long> findMemberIdsByCourseId(Long courseId);
}
