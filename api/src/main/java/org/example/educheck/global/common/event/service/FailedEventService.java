package org.example.educheck.global.common.event.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.educheck.domain.attendance.event.AbsenceApprovedEvent;
import org.example.educheck.domain.attendance.event.AttendanceUpdatedEvent;
import org.example.educheck.domain.attendance.service.AttendanceSummaryService;
import org.example.educheck.global.common.event.entity.FailedEvent;
import org.example.educheck.global.common.event.entity.Status;
import org.example.educheck.global.common.event.repository.FailedEventRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.fasterxml.jackson.databind.type.LogicalType.Map;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class FailedEventService {

    private final FailedEventRepository failedEventRepository;
    private final AttendanceSummaryService attendanceSummaryService;
    private final ObjectMapper objectMapper;

    private static final int MAX_RETRY_COUNT = 3;

    @Transactional
    public void reprocessFailedEvents() {
        List<FailedEvent> unresolvedEvents = failedEventRepository.findByStatus(Status.UNRESOLVED);
        log.info("Unresolved events {} 건 재처리 시작", unresolvedEvents.size());

        for (FailedEvent failedEvent : unresolvedEvents) {
            try {
                switch (failedEvent.getEventType()) {
                    case "AttendanceUpdatedEvent":
                        AttendanceUpdatedEvent attendanceEvent = objectMapper.readValue(failedEvent.getPayload(),  AttendanceUpdatedEvent.class);
                        Long courseId = attendanceEvent.getAttendance().getLecture().getCourse().getId();
                        Long studentId = attendanceEvent.getAttendance().getStudent().getId();
                        attendanceSummaryService.recalculateAttendanceSummarySync(studentId, courseId);
                        break;
                    case "AbsenceApprovedEvent":
                        AbsenceApprovedEvent absenceApprovedEvent = objectMapper.readValue(failedEvent.getPayload(), AbsenceApprovedEvent.class);
                        attendanceSummaryService.recalculateAttendanceSummarySync(absenceApprovedEvent.getStudentId(), absenceApprovedEvent.getCourseId());
                        break;
                    default:
                        failedEvent.updateStatus(Status.IGNORED);
                        log.warn("알 수 없는 이벤트 타입 : {}", failedEvent.getEventType());
                        continue;
                }

                failedEvent.updateStatus(Status.RESOLVED);
                log.info("이벤트 ID {} ({}) 재처리 성공", failedEvent.getId(), failedEvent.getEventType());

            } catch (Exception e) {
                failedEvent.incrementRetryCount();
                log.error("이벤트 ID {} 재처리 실패, 재시도 횟수 : {}/{}", failedEvent.getId(), failedEvent.getRetryCount(), MAX_RETRY_COUNT);

                if (failedEvent.getRetryCount() >= MAX_RETRY_COUNT) {
                    failedEvent.updateStatus(Status.IGNORED);
                    log.warn("이벤트 ID {} 가 최대 재시도 횟수 ({})에 도달하여 IGNORED 처리", failedEvent.getId(), MAX_RETRY_COUNT);
                }
            }
        }
    }

    @Scheduled(cron = "0 0 2 * * *")
    public void scheduleFailedEventsReprocessing() {
        log.info("스케줄러 실행 : 실패한 이벤트 재처리 시작");
        reprocessFailedEvents();
    }
}
