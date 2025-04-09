package org.example.educheck.global.common.time;

import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDateTime;

@Component
public class SystemTimeProvider {

    private final Clock clock;

    public SystemTimeProvider() {
        this.clock = Clock.systemDefaultZone();
    }

    public LocalDateTime now() {
        return LocalDateTime.now(clock);
    }
}
