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
import org.example.educheck.domain.member.repository.MemberRepository;
import org.example.educheck.global.common.exception.custom.common.InvalidRequestException;
import org.example.educheck.global.common.exception.custom.common.ResourceMismatchException;
import org.example.educheck.global.common.exception.custom.common.ResourceNotFoundException;
import org.example.educheck.global.common.exception.custom.common.ResourceOwnerMismatchException;
import org.example.educheck.global.common.exception.custom.reservation.ReservationConflictException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MeetingRoomReservationService {

    private final MeetingRoomReservationRepository meetingRoomReservationRepository;
    private final MemberRepository memberRepository;
    private final MeetingRoomRepository meetingRoomRepository;
    private final MemberReservationPolicy memberReservationPolicy;
    private final MeetingRoomReservationPolicy meetingRoomReservationPolicy;



    @Transactional
    public MeetingRoomReservationResponseDto createReservation(Member member, Long campusId, MeetingRoomReservationRequestDto requestDto) {

        LocalDateTime startTime = requestDto.getStartTime();
        LocalDateTime endTime = requestDto.getEndTime();
        Long memberId = member.getId();

        MeetingRoom meetingRoom = meetingRoomRepository.findById(requestDto.getMeetingRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("해당 회의실이 존재하지 않습니다."));

        meetingRoom.validateBelongsToCampus(campusId);
        meetingRoomReservationPolicy.validateReservableTime(meetingRoom, startTime, endTime);

        memberReservationPolicy.validateDailyReservationLimit(memberId, startTime, endTime);
        memberReservationPolicy.validateReservedAtSameTime(memberId, startTime, endTime);

        MeetingRoomReservationTime reservationTime = MeetingRoomReservationTime.of(startTime, endTime);
        MeetingRoomReservation meetingRoomReservation = MeetingRoomReservation.create(member, meetingRoom, reservationTime);

        return MeetingRoomReservationResponseDto.from(meetingRoomReservationRepository.save(meetingRoomReservation));
    }

    public MeetingRoomReservationResponseDto getMeetingRoomReservationById(Long reservationId) {
        MeetingRoomReservation meetingRoomReservation = meetingRoomReservationRepository.findByIdWithDetails(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("해당 예약 내역이 존재하지 않습니다."));

        return MeetingRoomReservationResponseDto.from(meetingRoomReservation);


    }

    @Transactional
    public void cancelReservation(Member member, Long meetingRoomReservationId) {

        MeetingRoomReservation meetingRoomReservation = meetingRoomReservationRepository.findByStatusAndById(meetingRoomReservationId, ReservationStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("해당 예약 내역이 존재하지 않습니다."));

        meetingRoomReservation.cancel(member, meetingRoomReservationPolicy);
    }

    public CampusMeetingRoomsDto getMeetingRoomReservations(Long campusId, LocalDate date) {

        List<MeetingRoomReservationsProjections> reservationsByCampus = meetingRoomReservationRepository.findByCampusId(campusId, date);

        Map<Long, MeetingRoomDto> meetingRoomDtoMap = new LinkedHashMap<>();

        for (MeetingRoomReservationsProjections reservation : reservationsByCampus) {
            Long meetingRoomId = reservation.getMeetingRoomId();
            String meetingRoomName = reservation.getMeetingRoomName();

            log.info("reservationId, reservationStartTime : {}, {}", reservation.getMeetingRoomReservationId(), reservation.getStartTime());

            meetingRoomDtoMap.putIfAbsent(meetingRoomId, new MeetingRoomDto(meetingRoomId, meetingRoomName, new ArrayList<>()));

            if (reservation.getMeetingRoomReservationId() != null) {
                meetingRoomDtoMap.get(meetingRoomId).getReservations().add(
                        new ReservationDto(reservation.getMeetingRoomReservationId(),
                                reservation.getMemberId(),
                                reservation.getMemberName(),
                                reservation.getStartTime(),
                                reservation.getEndTime())
                );
            }

        }

        return new CampusMeetingRoomsDto(campusId, LocalDate.now(), new ArrayList<>(meetingRoomDtoMap.values()));
    }
}
