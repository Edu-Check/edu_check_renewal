package org.example.educheck.domain.attendance.event;

import java.util.Map;

public interface FailedEventPayloadProvider {
    Map<String,Object> toFailedEventPayload();
}
