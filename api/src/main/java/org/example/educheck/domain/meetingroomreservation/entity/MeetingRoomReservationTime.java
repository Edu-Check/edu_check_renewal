package org.example.educheck.domain.meetingroomreservation.entity;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.educheck.global.common.exception.custom.common.InvalidRequestException;
import org.example.educheck.global.common.exception.custom.reservation.ReservationConflictException;
import org.springframework.cglib.core.Local;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Embeddable
public class MeetingRoomReservationTime {

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @Builder
    private MeetingRoomReservationTime(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) {
            throw new InvalidRequestException("시작 시간과 종료 시간은 필수입니다.");
        }

        this.startTime = startTime;
        this.endTime = endTime;

        validateReservableTime();
    }

    public static MeetingRoomReservationTime of(LocalDateTime startTime, LocalDateTime endTime) {
        return MeetingRoomReservationTime.builder()
                .startTime(startTime)
                .endTime(endTime)
                .build();
    }

    private void validateReservableTime() {
        LocalTime startOfDay = LocalTime.of(9, 0);
        LocalTime endOfDay = LocalTime.of(22, 0);
        LocalDate today = LocalDate.now();

//        if (!startTime.toLocalDate().isEqual(today) || !endTime.toLocalDate().isEqual(today)) {
//            throw new InvalidRequestException("당일 예약만 가능합니다.");
//        }

        if (startTime.isBefore(LocalDateTime.now())) {
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

        if (startTime.toLocalTime().isBefore(startOfDay) || endTime.toLocalTime().isAfter(endOfDay)) {
            throw new InvalidRequestException("예약 가능 시간은 오전 9시부터 오후 10시까지입니다.");
        }
    }


    public void validateCancelable() {
        if (!isFinished()) {
            throw new InvalidRequestException("예약 종료 시간 이전에만 취소가 가능합니다.");
        }
    }

    public boolean isFinished() {
        return endTime.isBefore(LocalDateTime.now());
    }


}
