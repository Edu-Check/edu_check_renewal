package org.example.educheck.domain.campus;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Campus {
    private static final int ATTENDANCE_METER = 5000;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String contact;

    private double gpsX;
    private double gpsY;

    @Builder
    public Campus(String name, String contact, double gpsX, double gpsY) {
        this.name = name;
        this.contact = contact;
        this.gpsX = gpsX;
        this.gpsY = gpsY;
    }

    public boolean isWithinDistance(double latitude, double longitude) {

        double metersPerDegree = 111000;
        double dx = (longitude - this.gpsX) * metersPerDegree * Math.cos(Math.toRadians(latitude));
        double dy = (latitude - this.gpsY) * metersPerDegree;
        double distance = Math.sqrt(dx * dx + dy * dy);
        return distance <= ATTENDANCE_METER;
    }
}
