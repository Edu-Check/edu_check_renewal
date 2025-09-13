package org.example.educheck.domain.attendance.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.Map;

@Getter
@RequiredArgsConstructor
public class AbsenceApprovedEvent implements FailedEventPayloadProvider {

    private final Long courseId;
    private final Long studentId;
    private final LocalDate startDate;
    private final LocalDate endDate;

    @Override
    public Map<String, Object> toFailedEventPayload() {
        return Map.of(
                "studentId", studentId,
                "courseId", courseId
        );
    }
}
