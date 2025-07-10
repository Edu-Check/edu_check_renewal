package org.example.educheck.domain.attendance.repository;

import org.example.educheck.domain.attendance.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;


public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    @Query("SELECT a FROM Attendance a WHERE a.student.id = :studentId AND a.lecture.id = :lectureId")
    Optional<Attendance> findByStudentIdAndLectureId(Long studentId, Long lectureId);

    @Query("SELECT a FROM Attendance a " +
            "WHERE a.student.id = :studentId " +
            "AND a.checkInTimestamp IS NOT NULL " +
            "AND DATE(a.checkInTimestamp) = DATE(now())")
    Optional<Attendance> findByStudentIdTodayCheckInDate(@Param("studentId") Long studentId);

}
