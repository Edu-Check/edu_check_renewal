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
    public Attendance(Student student, Lecture lecture, LocalDateTime checkInTimestamp, AttendanceStatus attendanceStatus) {
        this.student = student;
        this.lecture = lecture;
        this.checkInTimestamp = checkInTimestamp;
        this.attendanceStatus = attendanceStatus;
    }


    public static Attendance checkIn(Student student, Lecture lecture, LocalDateTime nowDateTime) {
        LocalTime nowTime = nowDateTime.toLocalTime();
        LocalTime lectureStart = lecture.getStartTime();
        LocalTime lectureEnd = lecture.getEndTime();


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
                .checkInTimestamp(LocalDateTime.now())
                .attendanceStatus(status)
                .build();
    }

    public Attendance checkOut() {
        this.checkOutTimestamp = LocalDateTime.now();

        return this;
    }

}
