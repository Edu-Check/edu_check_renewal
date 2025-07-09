package org.example.educheck.domain.attendance.event;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.educheck.domain.attendance.entity.Attendance;
import org.example.educheck.domain.attendance.entity.AttendanceStatus;

@Getter
@RequiredArgsConstructor
public class AttendanceUpdateEvent {

    private final Attendance attendance;

    private final AttendanceStatus oldStatus;

    private final AttendanceStatus newStatus;

}
