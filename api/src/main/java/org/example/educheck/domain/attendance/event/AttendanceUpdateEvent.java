package org.example.educheck.domain.attendance.event;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.educheck.domain.attendance.entity.Attendance;

@RequiredArgsConstructor
public class AttendanceUpdateEvent {

    @Getter
    private final Attendance attendance;

}
