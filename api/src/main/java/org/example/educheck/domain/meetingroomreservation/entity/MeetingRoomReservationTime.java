package org.example.educheck.domain.meetingroomreservation.entity;

import jakarta.persistence.Embeddable;
import lombok.*;
import org.example.educheck.global.common.exception.custom.common.InvalidRequestException;
import org.example.educheck.global.common.exception.custom.reservation.ReservationConflictException;
import org.example.educheck.global.common.time.SystemTimeProvider;
import org.springframework.cglib.core.Local;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Embeddable
public class MeetingRoomReservationTime {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("a h시 m분").withLocale(Locale.KOREAN);
    private static final LocalTime START_OF_DAY = LocalTime.of(9,0);
    private static final LocalTime END_OF_DAY = LocalTime.of(22, 0);
    private static final String RESERVATION_AVAILABLE_TIME_MESSAGE = String.format(
            "예약 가능 시간은 %s부터 %s까지입니다. ",
            START_OF_DAY.format(TIME_FORMATTER),
            END_OF_DAY.format(TIME_FORMATTER)
    );

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    public static MeetingRoomReservationTime of(LocalDateTime startTime, LocalDateTime endTime, LocalDateTime now) {
        validateReservableTime(startTime, endTime, now);
        return new MeetingRoomReservationTime(startTime, endTime);
    }

    private static void validateReservableTime(LocalDateTime startTime, LocalDateTime endTime, LocalDateTime now) {

        LocalDate currentDate = now.toLocalDate();

        if (isNotSameDay(startTime, currentDate) || isNotSameDay(endTime, currentDate)) {
            throw new InvalidRequestException("당일 예약만 가능합니다.");
        }

        if (startTime.isBefore(now)) {
            throw new InvalidRequestException("현재 시간 이후로만 예약 가능합니다.");
        }

        if (endTime.isBefore(startTime)) {
            throw new InvalidRequestException("시작 시간이 종료 시간보다 늦을 수 없습니다.");
        }

        if (startTime.isAfter(endTime)) {
            throw new InvalidRequestException("종료 시간이 시작 시간보다 빠를 수 없습니다.");
        }

        if (ChronoUnit.MINUTES.between(startTime, endTime) < 15) {
            throw new InvalidRequestException("최소 예약 시간은 15분입니다.");
        }

        if (startTime.toLocalTime().isBefore(START_OF_DAY) || endTime.toLocalTime().isAfter(END_OF_DAY)) {
            throw new InvalidRequestException(RESERVATION_AVAILABLE_TIME_MESSAGE);
        }
    }

    private static boolean isNotSameDay(LocalDateTime startTime, LocalDate currentDate) {
        return !startTime.toLocalDate().isEqual(currentDate);
    }

}
