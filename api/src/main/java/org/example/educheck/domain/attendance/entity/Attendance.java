package org.example.educheck.domain.attendance.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.educheck.domain.lecture.entity.Lecture;
import org.example.educheck.domain.member.student.entity.Student;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;


@Entity
@Getter
@Setter
@NoArgsConstructor
public class Attendance {
    protected static final int ATTENDANCE_DEADLINE_MILLI_SECONDS = 1800000;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecture_id")
    private Lecture lecture;

    private LocalDateTime checkInTimestamp;
    private LocalDateTime checkOutTimestamp;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "VARCHAR(50)")
    private AttendanceStatus attendanceStatus;

    @Builder
    public Attendance(Student student, Lecture lecture, LocalDateTime checkInTimestamp, LocalDateTime checkOutTimestamp, AttendanceStatus attendanceStatus) {
        this.student = student;
        this.lecture = lecture;
        this.checkInTimestamp = checkInTimestamp;
        this.checkOutTimestamp = checkOutTimestamp;
        this.attendanceStatus = attendanceStatus;
    }


    public static Attendance checkIn(Student student, Lecture lecture, LocalDateTime nowDateTime) {
        LocalTime lectureStart = lecture.getStartTime();
        LocalTime lectureEnd = lecture.getEndTime();
        LocalTime nowTime = nowDateTime.toLocalTime();

        if (nowTime.isAfter(lectureEnd)) {
            throw new IllegalArgumentException("출석 가능한 시간이 아닙니다.");
        }

        long diffMilliSeconds = Duration.between(lectureStart, nowTime).toMillis();
        AttendanceStatus status = diffMilliSeconds < ATTENDANCE_DEADLINE_MILLI_SECONDS
                ? AttendanceStatus.ATTENDANCE
                : AttendanceStatus.LATE;

        return Attendance.builder()
                .student(student)
                .lecture(lecture)
                .checkInTimestamp(nowDateTime)
                .attendanceStatus(status)
                .build();
    }

    public void checkOut(LocalDateTime nowDateTime) {

        if (checkInTimestamp == null) {
            throw new IllegalArgumentException("금일 출석 기록이 없습니다.");
        }
        checkOutTimestamp = nowDateTime;

        if (lecture.isBeforeLectureEndTime(nowDateTime.toLocalTime())) {
            if (attendanceStatus == AttendanceStatus.LATE) {
                attendanceStatus = null; // TODO: 결석
            } else {
                attendanceStatus = AttendanceStatus.EARLY_LEAVE;
            }
        }
    }
}
