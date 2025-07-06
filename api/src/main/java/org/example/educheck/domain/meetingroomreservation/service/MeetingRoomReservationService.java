package org.example.educheck.domain.meetingroomreservation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.educheck.domain.meetingroom.entity.MeetingRoom;
import org.example.educheck.domain.meetingroom.repository.MeetingRoomRepository;
import org.example.educheck.domain.meetingroomreservation.dto.request.MeetingRoomReservationRequestDto;
import org.example.educheck.domain.meetingroomreservation.dto.response.*;
import org.example.educheck.domain.meetingroomreservation.entity.MeetingRoomReservation;
import org.example.educheck.domain.meetingroomreservation.entity.MeetingRoomReservationTime;
import org.example.educheck.domain.meetingroomreservation.entity.ReservationStatus;
import org.example.educheck.domain.meetingroomreservation.policy.MeetingRoomReservationPolicy;
import org.example.educheck.domain.meetingroomreservation.policy.MemberReservationPolicy;
import org.example.educheck.domain.meetingroomreservation.repository.MeetingRoomReservationRepository;
import org.example.educheck.domain.member.entity.Member;
import org.example.educheck.global.common.exception.custom.common.ResourceNotFoundException;
import org.example.educheck.global.common.time.SystemTimeProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MeetingRoomReservationService {

    private final MeetingRoomReservationRepository meetingRoomReservationRepository;
    private final MeetingRoomRepository meetingRoomRepository;
    private final MemberReservationPolicy memberReservationPolicy;
    private final MeetingRoomReservationPolicy meetingRoomReservationPolicy;
    private final SystemTimeProvider systemTimeProvider;



    @Transactional
    public MeetingRoomReservationResponseDto createReservation(Member member, Long campusId, MeetingRoomReservationRequestDto requestDto) {
        LocalDateTime now = systemTimeProvider.nowDateTime();
        LocalDateTime startTime = requestDto.getStartTime();
        LocalDateTime endTime = requestDto.getEndTime();

        MeetingRoom meetingRoom = meetingRoomRepository.findByIdWithOptimisticLock(requestDto.getMeetingRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("해당 회의실이 존재하지 않습니다."));

        meetingRoom.validateBelongsToCampus(campusId);

        meetingRoomReservationPolicy.validateReservableTime(meetingRoom, startTime, endTime);

        MeetingRoomReservationTime reservationTime = MeetingRoomReservationTime.create(startTime, endTime, now);
        MeetingRoomReservation meetingRoomReservation = MeetingRoomReservation.create(member, meetingRoom, reservationTime, meetingRoomReservationPolicy, memberReservationPolicy);

        return MeetingRoomReservationResponseDto.from(meetingRoomReservationRepository.save(meetingRoomReservation));
    }

    public MeetingRoomReservationResponseDto getMeetingRoomReservationDetails(Long reservationId) {

        MeetingRoomReservation meetingRoomReservation = meetingRoomReservationRepository.findActiveByIdWithDetails(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("해당 예약 내역이 존재하지 않습니다."));

        return MeetingRoomReservationResponseDto.from(meetingRoomReservation);


    }

    @Transactional
    public void cancelReservation(Member member, Long meetingRoomReservationId) {

        LocalDateTime now = systemTimeProvider.nowDateTime();

        MeetingRoomReservation meetingRoomReservation = meetingRoomReservationRepository.findByStatusAndById(meetingRoomReservationId, ReservationStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("해당 예약 내역이 존재하지 않습니다."));

        meetingRoomReservation.cancel(member, meetingRoomReservationPolicy, now);
    }

    public CampusMeetingRoomsDto getMeetingRoomReservations(Long campusId, LocalDate date) {

        List<MeetingRoomReservationsProjection> reservationProjections = meetingRoomReservationRepository.findByCampusId(campusId, date);

        Map<Long, MeetingRoomDto> meetingRoomDtoMap = new LinkedHashMap<>();

        for (MeetingRoomReservationsProjection projection : reservationProjections) {
            Long meetingRoomId = projection.getMeetingRoomId();

            meetingRoomDtoMap.putIfAbsent(meetingRoomId, createEmptyMeetingRoomDto(projection));

            if (projection.getMeetingRoomReservationId() != null) {
                ReservationDto reservationDto = ReservationDto.from(projection);
                meetingRoomDtoMap.get(meetingRoomId).getReservations().add(reservationDto);
            }
        }

        return new CampusMeetingRoomsDto(campusId, LocalDate.now(), new ArrayList<>(meetingRoomDtoMap.values()));
    }

    private MeetingRoomDto createEmptyMeetingRoomDto(MeetingRoomReservationsProjection projection) {
        return new MeetingRoomDto(projection.getMeetingRoomId(), projection.getMeetingRoomName(), new ArrayList<>());
    }

}
