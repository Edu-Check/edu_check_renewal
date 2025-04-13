package org.example.educheck.domain.meetingroomreservation.policy;

import lombok.RequiredArgsConstructor;
import org.example.educheck.domain.meetingroom.repository.MeetingRoomRepository;
import org.example.educheck.domain.meetingroomreservation.dto.request.MeetingRoomReservationRequestDto;
import org.example.educheck.domain.meetingroomreservation.entity.ReservationStatus;
import org.example.educheck.domain.meetingroomreservation.repository.MeetingRoomReservationRepository;
import org.example.educheck.global.common.exception.custom.common.InvalidRequestException;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

@RequiredArgsConstructor
@Component
public class MemberReservationPolicy {

    private static final int DAILY_RESERVATION_LIMIT_MINUTES = 120;
    private static final String DAILY_RESERVATION_LIMIT_MESSAGE =
            String.format("하루에 총 %d분 예약할 수 있습니다.", DAILY_RESERVATION_LIMIT_MINUTES);

    private final MeetingRoomReservationRepository meetingRoomReservationRepository;

    public void validateReservedAtSameTime(Long memberId, LocalDateTime startTime, LocalDateTime endTime) {
        LocalDate date = startTime.toLocalDate();
        boolean result = meetingRoomReservationRepository.existsMemberReservationAtSameTime(
                memberId, date, startTime, endTime, ReservationStatus.ACTIVE);

        if (result) {
            throw new InvalidRequestException("같은 시간대에 여러 회의실을 예약할 수 없습니다.");
        }
    }

    public void validateDailyReservationLimit(Long memberId, LocalDateTime startTime, LocalDateTime endTime) {
        int todayReservedMinutes = meetingRoomReservationRepository.getTotalReservationMinutesForMember(memberId);

        int requestMinutes = (int) Duration.between(startTime, endTime).toMinutes();
        int totalMinAfterRequest = requestMinutes + todayReservedMinutes;
        int availableTime = DAILY_RESERVATION_LIMIT_MINUTES - todayReservedMinutes;

        if (totalMinAfterRequest > DAILY_RESERVATION_LIMIT_MINUTES) {
            throw new InvalidRequestException(String.format("%s 오늘 가능한 시간은 %d분 입니다.", DAILY_RESERVATION_LIMIT_MESSAGE, availableTime));
        }
    }
}
