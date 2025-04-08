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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campus_id")
    private Campus campus;

    private String name;

    public Long getCampusId() {
        return this.campus != null ? this.campus.getId() : null;
    }

    public void validateBelongsToCampus(Long campusId) {
        if (!this.campus.getId().equals(campusId)) {
            throw new ResourceMismatchException("해당 회의실은 캠퍼스 내 회의실이 아닙니다.");
        }
    }
}
