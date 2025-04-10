package org.example.educheck.domain.attendanceRegister.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.educheck.domain.attendance.entity.AttendanceStatus;
import org.hibernate.annotations.Immutable;
import org.springframework.data.annotation.Id;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Immutable
@Entity
public class AttendanceRegister {

    @EmbeddedId
    private AttendanceRegisterId id;

    @Column(name = "member_id")
    private Long memberId;

    @Column(name = "student_name")
    private String studentName;

    @Column(name = "student_email")
    private String studentEmail;

    @Column(name = "student_phone_number")
    private String studentPhoneNumber;

    @Column(name = "lecture_session")
    private Integer lectureSession;

    @Column(name = "lecture_date")
    private LocalDate lectureDate;

    @Column(name = "lecture_start_time")
    private LocalTime lectureStartTime;

    @Column(name = "lecture_end_time")
    private LocalTime lectureEndTime;

    @Column(name = "course_participation_status")
    private String courseParticipationStatus;

    @Column(name = "check_in_timestamp")
    private LocalDateTime checkInTimestamp;

    @Column(name = "check_out_timestamp")
    private LocalDateTime checkOutTimestamp;

    @Enumerated(EnumType.STRING)
    @Column(name = "attendance_status")
    private AttendanceStatus attendanceStatus;

    /**
     * Registration 테이블의 drop_date, completion_date 컬럼 값에 따라 상태값 판단
     * - DROPPED, COMPLETED, ACTIVE
     */
    @Column(name = "registration_status")
    private String registrationStatus;

    @Override
    public String toString() {
        return "AttendanceRegister{" +
                "id=" + id +
                ", memberId=" + memberId +
                ", studentName='" + studentName + '\'' +
                ", studentEmail='" + studentEmail + '\'' +
                ", lectureSession=" + lectureSession +
                ", lectureDate=" + lectureDate +
                ", lectureStartTime=" + lectureStartTime +
                ", lectureEndTime=" + lectureEndTime +
                ", courseParticipationStatus='" + courseParticipationStatus + '\'' +
                ", checkInTimestamp=" + checkInTimestamp +
                ", checkOutTimestamp=" + checkOutTimestamp +
                ", attendanceStatus='" + attendanceStatus + '\'' +
                ", registrationStatus='" + registrationStatus + '\'' +
                '}';
    }
}
