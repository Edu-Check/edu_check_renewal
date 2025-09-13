package org.example.educheck.domain.attendance.event;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.educheck.domain.attendance.entity.Attendance;
import org.example.educheck.domain.attendance.entity.AttendanceStatus;

import java.util.Map;

@Getter
@RequiredArgsConstructor
public class AttendanceUpdatedEvent implements FailedEventPayloadProvider {

    private final Long studentId;
    private final Long courseId;
    private final Long attendanceId;

    @Override
    public Map<String, Object> toFailedEventPayload() {
        return Map.of(
                "studentId", studentId,
                "courseId", courseId,
                "attendanceId", attendanceId
        );
    }
}
