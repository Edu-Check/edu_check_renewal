package org.example.educheck.domain.event.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.educheck.domain.absenceattendance.event.AbsenceApprovedEvent;
import org.example.educheck.domain.attendance.event.AttendanceUpdatedEvent;
import org.example.educheck.domain.attendance.service.AttendanceSummaryService;
import org.example.educheck.domain.event.entity.FailedEvent;
import org.example.educheck.domain.event.entity.Status;
import org.example.educheck.domain.event.repository.FailedEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class FailedEventService {

    private static final Logger log = LoggerFactory.getLogger(FailedEventService.class);
    private final FailedEventRepository failedEventRepository;
    private final AttendanceSummaryService attendanceSummaryService;
    private final ObjectMapper objectMapper;

    @Transactional
    public void reprocessFailedEvents() {
        List<FailedEvent> unresolvedEvents = failedEventRepository.findByStatus(Status.UNRESOLVED);
        log.info("Unresolved events {} 건 재처리 시작", unresolvedEvents.size());

        for (FailedEvent failedEvent : unresolvedEvents) {
            try {
                switch (failedEvent.getEventTYPE()) {
                    case "AttendanceUpdatedEvent":
                        AttendanceUpdatedEvent attendanceEvent = objectMapper.readValue(failedEvent.getPayload(), AttendanceUpdatedEvent.class);
                        attendanceSummaryService.handleAttendanceUpdatedEvent(attendanceEvent);
                        break;
                    case "AbsenceApprovedEvent":
                        AbsenceApprovedEvent absenceApprovedEvent = objectMapper.readValue(failedEvent.getPayload(), AbsenceApprovedEvent.class);
                        attendanceSummaryService.handleAbsenceApprovedEvent(absenceApprovedEvent);
                        break;
                    default:
                        log.warn("알 수 없는 이벤트 타입 : {}", failedEvent.getEventTYPE());
                        continue;
                }

                failedEvent.updateStatus(Status.RESOLVED);
                log.info("이벤트 ID {} ({}) 재처리 성공", failedEvent.getId(), failedEvent.getEventTYPE());

            } catch (Exception e) {
                log.error("이벤트 ID {} 재처리 실패, 에러 : {}", failedEvent.getId(), e.getMessage());
                //TODO : 재처리 마저 오류나면 어떻게 해야하지?
            }
        }
    }

    @Scheduled(cron = "0 0 2 * * *")
    public void scheduleFailedEventsReprocessing() {
        log.info("스케줄러 실행 : 실패한 이벤트 재처리 시작");
        reprocessFailedEvents();
    }
}
