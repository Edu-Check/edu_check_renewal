package org.example.educheck.domain.attendance.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.educheck.domain.absenceattendance.entity.AbsenceAttendance;
import org.example.educheck.domain.absenceattendance.event.AbsenceApprovedEvent;
import org.example.educheck.domain.absenceattendance.repository.AbsenceAttendanceRepository;
import org.example.educheck.domain.attendance.entity.Attendance;
import org.example.educheck.domain.attendance.entity.AttendanceStatus;
import org.example.educheck.domain.attendance.entity.AttendanceSummary;
import org.example.educheck.domain.attendance.entity.AttendanceSummaryId;
import org.example.educheck.domain.attendance.event.AttendanceUpdatedEvent;
import org.example.educheck.domain.attendance.repository.AttendanceRepository;
import org.example.educheck.domain.attendance.repository.AttendanceSummaryRepository;
import org.example.educheck.domain.lecture.entity.Lecture;
import org.example.educheck.domain.lecture.repository.LectureRepository;
import org.example.educheck.global.common.time.SystemTimeProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class AttendanceSummaryService {

    private final AttendanceSummaryRepository attendanceSummaryRepository;
    private final LectureRepository lectureRepository;
    private final SystemTimeProvider timeProvider;
    private final AttendanceRepository attendanceRepository;
    private final AbsenceAttendanceRepository absenceAttendanceRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMPLETION)
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
                    log.info("totalLecturesInCourse : {}", totalLecturesInCourse);
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

        // Always update lecture counts to ensure they are current
        summary.setTotalLectureCount(lectureRepository.countByCourseId(courseId));
        summary.setLectureCountUntilToday(lectureRepository.countByCourseIdAndDateLessThanEqual(courseId, timeProvider.nowDate()));

        LocalDate lectureDate = attendance.getLecture().getDate();
        boolean contributesToUntilToday = !lectureDate.isAfter(timeProvider.nowDate());

        if (contributesToUntilToday) {
            summary.updateSummary(oldStatus, newStatus);
        }

        attendanceSummaryRepository.save(summary);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMPLETION)
    public void handleAbsenceApprovedEvent(AbsenceApprovedEvent event) {

        log.info("유고 결석 승인 이벤트 수신 : id : {}, 과정 id : {}", event.getStudentId(), event.getCourseId());
        Long courseId = event.getCourseId();
        Long studentId = event.getStudentId();
        LocalDate starDate = event.getStarDate();
        LocalDate endDate = event.getEndDate();

        List<Lecture> allLectures = lectureRepository.findAllByCourseId(courseId);

        List<Attendance> attendances = attendanceRepository.findAllByStudentAndCourse(studentId, courseId);

        List<AbsenceAttendance> approvedAbsences = absenceAttendanceRepository.findApprovedAbsences(studentId, courseId);

        AttendanceSummaryId summaryId = new AttendanceSummaryId(studentId, courseId);
        AttendanceSummary summary = attendanceSummaryRepository.findById(summaryId)
                .orElseGet(() -> {
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
                });

        summary.recalculateWithApprovedAbsences(attendances, approvedAbsences, allLectures, timeProvider);

        log.info("summary : {}", summary.toString());

        attendanceSummaryRepository.save(summary);

    }

}
