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
    private static final int ATTENDANCE_METER = 100000; //TODO: 100키로-> 100m로 바꾸기
    private static final double EARTH_RADIUS = 6371000;

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

        double lat1 = Math.toRadians(this.gpsY);
        double lon1 = Math.toRadians(this.gpsX);
        double lat2 = Math.toRadians(latitude);
        double lon2 = Math.toRadians(longitude);


        double dLat = lat2 - lat1;
        double dLon = lon2 - lon1;


        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(lat1) * Math.cos(lat2) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = EARTH_RADIUS * c;


        return distance <= ATTENDANCE_METER;
    }
}
