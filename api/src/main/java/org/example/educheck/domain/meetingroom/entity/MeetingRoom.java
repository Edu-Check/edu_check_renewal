package org.example.educheck.domain.meetingroom.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.educheck.domain.campus.Campus;
import org.example.educheck.global.common.exception.custom.common.ResourceMismatchException;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MeetingRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version = 0L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campus_id")
    private Campus campus;

    private String name;

    public void validateBelongsToCampus(Long campusId) {
        if (isNotInCampus(campusId)) {
            throw new ResourceMismatchException("해당 회의실은 캠퍼스 내 회의실이 아닙니다.");
        }
    }

    public boolean isNotInCampus(Long campusId) {
        return !this.campus.isSameCampus(campusId);
    }

    @Builder
    public MeetingRoom(Campus campus, String name) {
        this.campus = campus;
        this.name = name;
    }
}
