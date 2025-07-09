package org.example.educheck.domain.attendance.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.educheck.domain.attendance.entity.Attendance;
import org.example.educheck.domain.attendance.entity.AttendanceStatus;
import org.example.educheck.domain.attendance.entity.AttendanceSummary;
import org.example.educheck.domain.attendance.entity.AttendanceSummaryId;
import org.example.educheck.domain.attendance.event.AttendanceUpdatedEvent;
import org.example.educheck.domain.attendance.repository.AttendanceSummaryRepository;
import org.example.educheck.domain.lecture.repository.LectureRepository;
import org.example.educheck.global.common.time.SystemTimeProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class AttendanceSummaryService {

    private final AttendanceSummaryRepository attendanceSummaryRepository;
    private final LectureRepository lectureRepository;
    private final SystemTimeProvider timeProvider;

    @Transactional
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleAttendanceUpdatedEvent(AttendanceUpdatedEvent event) {
        Attendance attendance = event.getAttendance();
        AttendanceStatus oldStatus = event.getOldStatus();
        AttendanceStatus newStatus = event.getNewStatus();

        Long studentId = attendance.getStudent().getId();
        Long courseId = attendance.getLecture().getCourse().getId();

        //TODO: builder 패턴이나 별도의 메서드로 수정하기
        AttendanceSummaryId summaryId = new AttendanceSummaryId(studentId, courseId);

        AttendanceSummary summary = attendanceSummaryRepository.findById(summaryId)
                .orElseGet(() -> {
                    Integer totalLecturesInCourse = lectureRepository.countByCourseId(courseId);
                    Integer lecturesUntilToday = lectureRepository.countByCourseIdAndDateLessThanEqual(courseId, timeProvider.nowDate());

                    if (totalLecturesInCourse == null) totalLecturesInCourse = 0;

                    return AttendanceSummary.builder()
                            .studentId(studentId)
                            .courseId(courseId)
                            .totalAttendanceRate(0.0)
                            .lateCountUntilToday(0)
                            .earlyLeaveCountUntilToday(0)
                            .absenceCountUntilToday(0)
                            .adjustedAbsenceCount(0)
                            .attendanceRateUntilToday(0.0)
                            .lectureCountUntilToday(lecturesUntilToday)
                            .attendanceCountUntilToday(0)
                            .adjustedAbsentByLateOrEarlyLeave(0)
                            .totalLectureCount(totalLecturesInCourse)
                            .build();
                });

        LocalDate lectureDate = attendance.getLecture().getDate();
        boolean contributesToUntilToday = !lectureDate.isAfter(timeProvider.nowDate());

        if (contributesToUntilToday) {
            summary.updateSummary(oldStatus, newStatus);
        }

        attendanceSummaryRepository.save(summary);
    }

}
