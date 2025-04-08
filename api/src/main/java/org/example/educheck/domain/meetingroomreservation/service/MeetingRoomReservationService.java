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

    private static void validateResourceOwner(Member authenticatedMember, MeetingRoomReservation meetingRoomReservation) {
        if (!authenticatedMember.getId().equals(meetingRoomReservation.getMember().getId())) {
            throw new ResourceOwnerMismatchException();
        }
    }

    @Transactional
    public MeetingRoomReservationResponseDto createReservation(Member member, Long campusId, MeetingRoomReservationRequestDto requestDto) {

        MeetingRoom meetingRoom = meetingRoomRepository.findById(requestDto.getMeetingRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("해당 회의실이 존재하지 않습니다."));

        meetingRoom.validateBelongsToCampus(campusId);

        MeetingRoomReservationTime reservationTime = MeetingRoomReservationTime.of(requestDto.getStartTime(), requestDto.getEndTime());

        validateDailyReservationLimit(member.getId(), requestDto);

        validateReservableTime(meetingRoom, requestDto.getStartTime(), requestDto.getEndTime());

        validateReservedAtSameTime(requestDto.getStartTime(), requestDto.getEndTime());

        MeetingRoomReservation meetingRoomReservation = requestDto.toEntity(member, meetingRoom, reservationTime);
        return MeetingRoomReservationResponseDto.from(meetingRoomReservationRepository.save(meetingRoomReservation));
    }

    private void validateDailyReservationLimit(Long memberId, MeetingRoomReservationRequestDto requestDto) {
        int todayReservedMinutes = meetingRoomReservationRepository.getTotalReservationMinutesForMember(memberId);

        int totalMinAfterRequest = (int) Duration.between(requestDto.getStartTime(), requestDto.getEndTime()).toMinutes() + todayReservedMinutes;
        int availableTime = 120 - todayReservedMinutes;

        if (totalMinAfterRequest > 120) {
            throw new InvalidRequestException(String.format("하루에 총 2시간까지 예약할 수 있습니다. 오늘 가능한 시간은 %d분 입니다.", availableTime));
        }
    }

    private Member getAuthenticatedMember(UserDetails user) {
        return memberRepository.findByEmail(user.getUsername()).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 member입니다."));
    }

    private void validateReservableTime(MeetingRoom meetingRoom, LocalDateTime startTime, LocalDateTime endTime) {
        LocalDate date = startTime.toLocalDate();
        boolean result = meetingRoomReservationRepository.existsOverlappingReservation(meetingRoom,
                date, startTime, endTime, ReservationStatus.ACTIVE);

        if (result) {
            throw new ReservationConflictException();
        }
    }


    private void validateReservedAtSameTime(LocalDateTime startTime, LocalDateTime endTime) {
        LocalDate date = startTime.toLocalDate();
        boolean result = meetingRoomReservationRepository.existsMemberReservationAtSameTime(
                date, startTime, endTime, ReservationStatus.ACTIVE);

        if (result) {
            throw new InvalidRequestException("같은 시간대에 여러 회의실을 예약할 수 없습니다.");
        }
    }

    private void validateUserCampusMatchMeetingRoom(Long campusId, MeetingRoom meetingRoom) {

        if (!campusId.equals(meetingRoom.getCampusId())) {
            throw new ResourceMismatchException("해당 회의실은 캠퍼스내 회의실이 아닙니다.");
        }
    }

    public MeetingRoomReservationResponseDto getMeetingRoomReservationById(Long reservationId) {
        MeetingRoomReservation meetingRoomReservation = meetingRoomReservationRepository.findByIdWithDetails(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("해당 예약 내역이 존재하지 않습니다."));

        return MeetingRoomReservationResponseDto.from(meetingRoomReservation);


    }

    @Transactional
    public void cancelReservation(UserDetails userDetails, Long meetingRoomReservationId) {

        MeetingRoomReservation meetingRoomReservation = meetingRoomReservationRepository.findByStatusAndById(meetingRoomReservationId, ReservationStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("해당 예약 내역이 존재하지 않습니다."));

        Member authenticatedMember = getAuthenticatedMember(userDetails);
        validateResourceOwner(authenticatedMember, meetingRoomReservation);

        validateEndTimeIsBeforeNow(meetingRoomReservation);

        meetingRoomReservation.cancelReservation();
        meetingRoomReservationRepository.save(meetingRoomReservation);
    }

    private void validateEndTimeIsBeforeNow(MeetingRoomReservation reservation) {
        reservation.getReservationTime().validateCancelable();
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
