package org.example.educheck.domain.attendanceRegister.repository;


import org.example.educheck.domain.attendanceRegister.entity.AttendanceRegister;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;


public interface AttendanceRegisterRepository extends JpaRepository<AttendanceRegister, Integer> {

    @Query("SELECT ar FROM AttendanceRegister ar " +
            "WHERE ar.id.courseId = :courseId " +
            "AND ar.lectureDate = CURRENT DATE")
    List<AttendanceRegister> findByCourseIdAndLectureDateIsToday(Long courseId);
}
