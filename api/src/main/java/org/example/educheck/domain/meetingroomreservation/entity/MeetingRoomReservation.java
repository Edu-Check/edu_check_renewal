package org.example.educheck.domain.meetingroomreservation.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.educheck.domain.meetingroom.entity.MeetingRoom;
import org.example.educheck.domain.member.entity.Member;
import org.example.educheck.global.common.entity.BaseTimeEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(
        name = "meeting_room_reservation",
        indexes = {
                @Index(name = "idx_meeting_room_availability", columnList = "meeting_room_id, start_time, end_time")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MeetingRoomReservation extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "memeber_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_room_id")
    private MeetingRoom meetingRoom;

    @Embedded
    private MeetingRoomReservationTime reservationTime;

    @Column(columnDefinition = "VARCHAR(20)")
    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

    public void cancelReservation() {
        this.status = ReservationStatus.CANCELED;
    }

    @Builder //TODO: DTO에서 사용하는거 삭제 후 빌더 삭제하기
    private MeetingRoomReservation(Member member,  MeetingRoom meetingRoom, MeetingRoomReservationTime reservationTime) {
        this.member = member;
        this.meetingRoom = meetingRoom;
        this.reservationTime = reservationTime;
        status = ReservationStatus.ACTIVE;
    }

    public static MeetingRoomReservation create(Member member,  MeetingRoom meetingRoom, MeetingRoomReservationTime reservationTime) {
        return new MeetingRoomReservation(member, meetingRoom, reservationTime);
    }
}
