package org.example.educheck.domain.attendance.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.educheck.domain.attendance.event.AbsenceApprovedEvent;
import org.example.educheck.domain.attendance.entity.Attendance;
import org.example.educheck.domain.attendance.event.AttendanceUpdatedEvent;
import org.example.educheck.domain.attendance.event.FailedEventPayloadProvider;
import org.example.educheck.global.common.event.entity.FailedEvent;
import org.example.educheck.global.common.event.repository.FailedEventRepository;
import org.example.educheck.global.common.event.service.FailedEventService;
import org.hibernate.PessimisticLockException;
import org.hibernate.dialect.lock.PessimisticEntityLockException;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class AttendanceSummaryService {

    private final AttendanceSummaryCalculator attendanceSummaryCalculator;
    private final FailedEventService failedEventService;

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Retryable(
            value = {
                    TransientDataAccessException.class,
                    CannotAcquireLockException.class,
                    OptimisticLockingFailureException.class,
                    PessimisticLockException.class
            },            maxAttempts = 4,
            backoff = @Backoff(
                    delay = 1500,
                    multiplier = 2
            )
    )
    public void handleAttendanceUpdatedEvent(AttendanceUpdatedEvent event) {

        Long studentId = event.getStudentId();
        Long courseId = event.getCourseId();

        log.info("출석 상태 변경 이벤트 수신 및 재계산 : 학생 id : {}, 과정 id : {}", studentId, courseId);

        attendanceSummaryCalculator.recalculateAttendanceSummary(studentId, courseId);
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Retryable(
            value = {
                    TransientDataAccessException.class,
                    CannotAcquireLockException.class,
                    OptimisticLockingFailureException.class,
                    PessimisticLockException.class
            },
            maxAttempts = 4,
            backoff = @Backoff(
                    delay = 1500,
                    multiplier = 2
            )
    )
    public void handleAbsenceApprovedEvent(AbsenceApprovedEvent event) {

        Long courseId = event.getCourseId();
        Long studentId = event.getStudentId();
        log.info("유고 결석 승인 이벤트 수신 및 재계산 : 학생 id : {}, 과정 id : {}", studentId, courseId);

        attendanceSummaryCalculator.recalculateAttendanceSummary(studentId, courseId);

    }

    @Transactional(readOnly = false)
    @Recover
    public void recover(Exception e, AttendanceUpdatedEvent event) {
        log.error("최종 실패 : AttendanceUpdatedEvent failed_event 테이블에 저장. event : {}", event, e);
        failedEventService.saveFailedEvent(event, e);
    }

    @Transactional(readOnly = false)
    @Recover
    public void recover(Exception e, AbsenceApprovedEvent event) {
        log.error("최종 실패 : AbsenceApprovedEvent failed_event 테이블에 저장. event : {}", event, e);
        failedEventService.saveFailedEvent(event, e);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false)
    public void recalculateAttendanceSummarySync(Long studentId, Long courseId) {
        attendanceSummaryCalculator.recalculateAttendanceSummary(studentId, courseId);
    }

}
