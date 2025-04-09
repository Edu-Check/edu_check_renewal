package org.example.educheck.domain.meetingroomreservation.policy;

import lombok.RequiredArgsConstructor;
import org.example.educheck.domain.meetingroom.entity.MeetingRoom;
import org.example.educheck.domain.meetingroomreservation.entity.ReservationStatus;
import org.example.educheck.domain.meetingroomreservation.repository.MeetingRoomReservationRepository;
import org.example.educheck.global.common.exception.custom.common.InvalidRequestException;
import org.example.educheck.global.common.exception.custom.reservation.ReservationConflictException;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class MeetingRoomReservationPolicy {

    private final MeetingRoomReservationRepository meetingRoomReservationRepository;

    /**
     * 한 회의실에 동시간대에 여러 예약을 할 수 없다.
     */
    public void validateReservableTime(MeetingRoom meetingRoom, LocalDateTime startTime, LocalDateTime endTime) {
        LocalDate date = startTime.toLocalDate();
        boolean result = meetingRoomReservationRepository.existsOverlappingReservation(meetingRoom,
                date, startTime, endTime, ReservationStatus.ACTIVE);

        if (result) {
            throw new ReservationConflictException();
        }
    }

    /**
     * 예약 종료 시간 이전에만 취소 가능하다.
     */
    public void validateCancelableTime(LocalDateTime endTime) {
        if (!isFinished(endTime)) {
            throw new InvalidRequestException("예약 종료 시간 이전에만 취소가 가능합니다.");
        }
    }

    private boolean isFinished(LocalDateTime endTime) {
        return endTime.isBefore(LocalDateTime.now());
    }




}
