package org.example.educheck.domain.attendance.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.educheck.domain.absenceattendance.entity.AbsenceAttendance;
import org.example.educheck.domain.absenceattendance.repository.AbsenceAttendanceRepository;
import org.example.educheck.domain.attendance.entity.Attendance;
import org.example.educheck.domain.attendance.entity.AttendanceSummary;
import org.example.educheck.domain.attendance.entity.AttendanceSummaryId;
import org.example.educheck.domain.attendance.repository.AttendanceRepository;
import org.example.educheck.domain.attendance.repository.AttendanceSummaryRepository;
import org.example.educheck.domain.lecture.entity.Lecture;
import org.example.educheck.domain.lecture.repository.LectureRepository;
import org.example.educheck.global.common.time.SystemTimeProvider;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AttendanceSummaryCalculator {

    private final AttendanceSummaryRepository attendanceSummaryRepository;
    private final LectureRepository lectureRepository;
    private final SystemTimeProvider timeProvider;
    private final AttendanceRepository attendanceRepository;
    private final AbsenceAttendanceRepository absenceAttendanceRepository;

    @Transactional
    public void recalculateAttendanceSummary(Long studentId, Long courseId) {
        log.info("출석 통계 테이블 재계산 실행 : 학생 id : {}, 과정 id : {}", studentId, courseId);

        List<Lecture> allLectures = lectureRepository.findAllByCourseId(courseId);
        List<Attendance> attendances = attendanceRepository.findAllByStudentAndCourse(studentId, courseId);
        List<AbsenceAttendance> approvedAbsences = absenceAttendanceRepository.findApprovedAbsences(studentId, courseId);

        AttendanceSummaryId summaryId = new AttendanceSummaryId(studentId, courseId);
        AttendanceSummary summary = attendanceSummaryRepository.findById(summaryId)
                .orElseGet(() -> createNewSummary(studentId, courseId));

        summary.recalculateWithApprovedAbsences(attendances, approvedAbsences, allLectures, timeProvider);
        attendanceSummaryRepository.save(summary);
    }

    private AttendanceSummary createNewSummary(Long studentId, Long courseId) {
        Integer totalLecturesInCourse = lectureRepository.countByCourseId(courseId);
        Integer lecturesUntilToday = lectureRepository.countByCourseIdAndDateLessThanEqual(courseId, timeProvider.nowDate());

        if (totalLecturesInCourse == null) totalLecturesInCourse = 0;

        return AttendanceSummary.builder()
                .studentId(studentId)
                .courseId(courseId)
                .totalLectureCount(totalLecturesInCourse)
                .lectureCountUntilToday(lecturesUntilToday)
                .lateCountUntilToday(0)
                .earlyLeaveCountUntilToday(0)
                .absenceCountUntilToday(0)
                .adjustedAbsenceCount(0)
                .attendanceRateUntilToday(0.0)
                .lectureCountUntilToday(lecturesUntilToday)
                .attendanceCountUntilToday(0)
                .build();
    }
}
