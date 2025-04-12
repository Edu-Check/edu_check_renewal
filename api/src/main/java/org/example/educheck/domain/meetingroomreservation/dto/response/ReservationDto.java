package org.example.educheck.domain.meetingroomreservation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class ReservationDto {

    private Long meetingRoomReservationId;
    private Long reserverId;
    private String reserverName;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;

    public static ReservationDto from(MeetingRoomReservationsProjection projection) {
        return ReservationDto.builder()
                .meetingRoomReservationId(projection.getMeetingRoomReservationId())
                .reserverId(projection.getMemberId())
                .reserverName(projection.getMemberName())
                .startDateTime(projection.getStartTime())
                .endDateTime(projection.getEndTime())
                .build();
    }
}
