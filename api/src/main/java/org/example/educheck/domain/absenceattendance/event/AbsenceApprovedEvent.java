package org.example.educheck.domain.absenceattendance.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;

@Getter
@RequiredArgsConstructor
public class AbsenceApprovedEvent {

    private final Long courseId;
    private final Long studentId;
    private final LocalDate starDate;
    private final LocalDate endDate;
}
