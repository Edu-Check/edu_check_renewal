package org.example.educheck.domain.meetingroomreservation.policy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.educheck.domain.meetingroom.entity.MeetingRoom;
import org.example.educheck.domain.meetingroomreservation.entity.ReservationStatus;
import org.example.educheck.domain.meetingroomreservation.repository.MeetingRoomReservationRepository;
import org.example.educheck.global.common.exception.custom.common.InvalidRequestException;
import org.example.educheck.global.common.exception.custom.reservation.ReservationConflictException;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
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
            throw new ReservationConflictException("해당 시간에는 이미 예약이 있습니다. 다른 시간을 선택해주세요.");
        }
    }

    /**
     * 예약 종료 시간 이전에만 취소 가능하다.
     */
    public void validateCancelableTime(LocalDateTime endTime, LocalDateTime now) {
        if (endTime.isBefore(now)) {
            throw new InvalidRequestException("예약 종료 시간 이전에만 취소가 가능합니다.");
        }
    }

}
