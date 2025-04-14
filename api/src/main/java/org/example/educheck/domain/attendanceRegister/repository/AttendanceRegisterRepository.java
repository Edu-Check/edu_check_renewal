package org.example.educheck.domain.attendanceRegister.repository;


import org.example.educheck.domain.attendanceRegister.dto.response.adminStudentDetail.AttendanceRateProjection;
import org.example.educheck.domain.attendanceRegister.dto.response.adminStudentDetail.AttendanceRecordResponseDto;
import org.example.educheck.domain.attendanceRegister.dto.response.myAttendanceRecord.MyAttendanceRecordProjection;
import org.example.educheck.domain.attendanceRegister.dto.response.myAttendanceStatics.MyAttendanceStaticsProjection;
import org.example.educheck.domain.attendanceRegister.entity.AttendanceRegister;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;


public interface AttendanceRegisterRepository extends JpaRepository<AttendanceRegister, Integer> {

    @Query("SELECT ar FROM AttendanceRegister ar " +
            "WHERE ar.id.courseId = :courseId " +
            "AND ar.lectureDate = CURRENT DATE")
    List<AttendanceRegister> findByCourseIdAndLectureDateIsToday(Long courseId);

    Page<AttendanceRecordResponseDto> findById_StudentIdAndId_CourseId(Long studentId, Long courseId, Pageable pageable);

    @Query(value = """
            WITH attendance_count AS (
            SELECT
                student_id, course_id,
                SUM(IF(attendance_status IN ('ATTENDANCE', 'LATE', 'EARLY_LEAVE') AND lecture_date <= CURDATE(), 1, 0)) AS attendance_count_until_today,
                SUM(IF(lecture_date <= CURDATE(),1,0)) AS lecture_count_until_today,
                COUNT(lecture_id) AS total_lecture_count,
                FLOOR(SUM(IF(attendance_status IN ('LATE', 'EARLY_LEAVE') AND lecture_date <= CURDATE(), 1, 0)) / 3) AS adjusted_absent_by_late_or_early_leave
            FROM attendance_register ar
            WHERE ar.student_id = :studentId AND ar.course_id = :courseId
            )
            SELECT
                IF(lecture_count_until_today > 0,
                    ROUND(
                        ((attendance_count_until_today - adjusted_absent_by_late_or_early_leave) / lecture_count_until_today) * 100,
                        2
                    ),
                    NULL
                ) AS attendance_rate_until_today,
                IF(
                    total_lecture_count > 0,
                    ROUND(
                        ((attendance_count_until_today - adjusted_absent_by_late_or_early_leave) / total_lecture_count) * 100,
                        2
                    ),
                    NULL
                ) AS total_attendance_rate,
                IF(total_lecture_count > 0,ROUND(lecture_count_until_today / total_lecture_count * 100, 2), NULL) AS course_progress_rate
            FROM attendance_count;
            """, nativeQuery = true)
    AttendanceRateProjection findAttendanceStatsByStudentIdAndCourseId(Long studentId, Long courseId);


    @Query(value = """
    WITH attendance_count AS (
        SELECT
            student_id, course_id,
            SUM(IF(attendance_status = 'ATTENDANCE' AND lecture_date <= CURDATE(),1,0)) AS attendance_count_until_today,
            SUM(IF(attendance_status = 'LATE' AND lecture_date <= CURDATE(), 1, 0)) AS late_count_until_today,
            SUM(IF(attendance_status = 'EARLY_LEAVE' AND lecture_date <= CURDATE(), 1, 0)) AS early_late_count_until_today,
            SUM(IF(attendance_status = 'ABSENCE' AND lecture_date <= CURDATE(), 1, 0)) AS absence_count_until_today,
            COUNT(lecture_id) AS total_lecture_count,
            FLOOR(SUM(IFNULL(IF(attendance_status IN ('LATE', 'EARLY_LEAVE') AND lecture_date <= CURDATE(), 1, 0), 0)) / 3) AS adjusted_absent_by_late_or_early_leave
        FROM attendance_register ar
        WHERE ar.student_id = :studentId AND ar.course_id = :courseId
    )
    SELECT
        IF(
            total_lecture_count > 0,
            ROUND(
                    ((attendance_count_until_today + late_count_until_today + early_late_count_until_today  - adjusted_absent_by_late_or_early_leave) / total_lecture_count) * 100,
                    2
                ),
                NULL
        ) AS total_attendance_rate,
        late_count_until_today,
        early_late_count_until_today,
        absence_count_until_today,
        FLOOR((late_count_until_today + early_late_count_until_today ) / 3) + absence_count_until_today AS adjusted_absence_count
    FROM attendance_count;
    """, nativeQuery = true)
    MyAttendanceStaticsProjection findAttendanceSummaryByStudentIdAndCourseId(Long studentId, Long courseId);

    @Query("SELECT ar FROM AttendanceRegister ar " +
            "WHERE ar.id.studentId = :studentId " +
            "AND ar.id.courseId = :courseId " +
            "AND ar.lectureDate BETWEEN :start AND :end")
    List<MyAttendanceRecordProjection> findByStudentIdAndCourseIdAndDateYearAndDateMonth(Long studentId, Long courseId, LocalDate start, LocalDate end);
}
