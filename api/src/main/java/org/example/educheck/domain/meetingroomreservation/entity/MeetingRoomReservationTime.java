package org.example.educheck.domain.meetingroomreservation.entity;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.educheck.global.common.exception.custom.common.InvalidRequestException;
import org.example.educheck.global.common.exception.custom.reservation.ReservationConflictException;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

@Getter
@NoArgsConstructor
@Embeddable
public class MeetingRoomReservationTime {

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    public MeetingRoomReservationTime(LocalDateTime startTime, LocalDateTime endTime) {
        LocalTime startOfDay = LocalTime.of(9, 0);
        LocalTime endOfDay = LocalTime.of(22, 0);


        if (endTime.isBefore(startTime)) {
            throw new ReservationConflictException("시작 시간이 종료 시간보다 늦을 수 없습니다.");
        }

        if (startTime.isAfter(endTime)) {
            throw new ReservationConflictException("종료 시간이 시작 시간보다 빠를 수 없습니다.");
        }

        if (ChronoUnit.MINUTES.between(startTime, endTime) < 15) {
            throw new ReservationConflictException("최소 예약 시간은 15분입니다.");
        }

        if (startTime.toLocalTime().isBefore(startOfDay) || endTime.toLocalTime().isAfter(endOfDay)) {
            throw new ReservationConflictException("예약 가능 시간은 오전 9시부터 오후 10시까지입니다.");
        }

        this.startTime = startTime;
        this.endTime = endTime;
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
