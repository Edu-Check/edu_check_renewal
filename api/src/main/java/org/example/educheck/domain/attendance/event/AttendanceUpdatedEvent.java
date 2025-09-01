package org.example.educheck.domain.attendance.event;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.educheck.domain.attendance.entity.Attendance;
import org.example.educheck.domain.attendance.entity.AttendanceStatus;

import java.util.Map;

@Getter
@RequiredArgsConstructor
public class AttendanceUpdatedEvent implements FailedEventPayloadProvider {

    private final Attendance attendance;

    private final AttendanceStatus oldStatus;

    private final AttendanceStatus newStatus;

    @Override
    public Map<String, Object> toFailedEventPayload() {
        return Map.of(
                "studentId", attendance.getStudent().getId(),
                "courseId", attendance.getLecture().getCourse().getId(),
                "attendanceId", attendance.getId()
        );
    }
}
