package org.example.educheck.domain.attendance.service;

import lombok.extern.slf4j.Slf4j;
import org.example.educheck.domain.attendance.entity.Attendance;
import org.example.educheck.domain.attendance.entity.AttendanceStatus;
import org.example.educheck.domain.attendance.entity.AttendanceSummary;
import org.example.educheck.domain.attendance.entity.AttendanceSummaryId;
import org.example.educheck.domain.attendance.event.AttendanceUpdatedEvent;
import org.example.educheck.domain.attendance.repository.AttendanceRepository;
import org.example.educheck.domain.attendance.repository.AttendanceSummaryRepository;
import org.example.educheck.domain.campus.Campus;
import org.example.educheck.domain.campus.repository.CampusRepository;
import org.example.educheck.domain.course.entity.Course;
import org.example.educheck.domain.course.entity.CourseStatus;
import org.example.educheck.domain.course.repository.CourseRepository;
import org.example.educheck.domain.lecture.entity.Lecture;
import org.example.educheck.domain.lecture.repository.LectureRepository;
import org.example.educheck.domain.member.entity.Member;
import org.example.educheck.domain.member.entity.Role;
import org.example.educheck.domain.member.repository.MemberRepository;
import org.example.educheck.domain.member.student.entity.Student;
import org.example.educheck.domain.member.student.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest
@Transactional
class AttendanceSummaryServiceTest {

    @Autowired
    private AttendanceSummaryService attendanceSummaryService;

    @Autowired
    private AttendanceSummaryRepository attendanceSummaryRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private LectureRepository lectureRepository;

    @Autowired
    private CampusRepository campusRepository;

    @Autowired
    private AttendanceService attendanceService;

    private Campus campus;
    private Member studentMember;
    private Student student;
    private Course course;
    private Lecture todayLecture;
    private Lecture yesterdayLecture;
    private Lecture nextdayLecture;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @BeforeEach
    void setUp() {
        campus = campusRepository.save(Campus.builder().name("Test Campus").build());

        studentMember = memberRepository.save(Member.builder()
                .name("Test Student")
                .role(Role.STUDENT)
                .email("abcde@naver.com")
                .build());

        student = studentRepository.save(Student.builder()
                .member(studentMember)
                .build());

        course = courseRepository.save(Course.builder()
                .name("Test Course5")
                .status(CourseStatus.PROCESSING)
                .startDate(LocalDate.now().minusDays(1))
                .endDate(LocalDate.now().plusDays(30))
                .campus(campus)
                .build());

        todayLecture = lectureRepository.save(Lecture.builder()
                .course(course)
                .session(1)
                .date(LocalDate.now())
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(18, 0))
                .build());

        // 오늘 이전의 강의 1개 추가
        yesterdayLecture = lectureRepository.save(Lecture.builder()
                .course(course)
                .session(2)
                .date(LocalDate.now().minusDays(1))
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(18, 0))
                .build());

        nextdayLecture = lectureRepository.save(Lecture.builder()
                .course(course)
                .session(3)
                .date(LocalDate.now().plusDays(1))
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(18, 0))
                .build());


    }

    //주 목적은 업데이트,  업데이트 이벤트가 호출된다는거니까
    @Test
    @DisplayName("새로운 출석 이벤트 발생 시 출석 통계가 정상적으로 생성 및 업데이트된다.")
    void handleNewAttendanceEvent() {

        log.info("Total Lectures: {}", lectureRepository.countByCourseId(course.getId()));

        // given
        Attendance newAttendance = Attendance.builder()
                .student(student)
                .lecture(todayLecture)
                .attendanceStatus(AttendanceStatus.ATTENDANCE)
                .build();
        Attendance savedAttendance = attendanceRepository.save(newAttendance);

        TestTransaction.flagForCommit(); //강제로 커밋 처리
        TestTransaction.end(); // 현재 트랜잭션을 커밋 후 종료
        TestTransaction.start(); // 다음 작업을 위해 새로운 트랜잭션 시작

        AttendanceUpdatedEvent event = new AttendanceUpdatedEvent(savedAttendance, null, savedAttendance.getAttendanceStatus());

        // when
        attendanceSummaryService.handleAttendanceUpdatedEvent(event);

        // then
        AttendanceSummary summary = attendanceSummaryRepository.findById(new AttendanceSummaryId(student.getId(), course.getId()))
                .orElse(null);

        assertThat(summary).isNotNull();
        assertThat(summary.getAttendanceCountUntilToday()).isEqualTo(1);
        assertThat(summary.getLateCountUntilToday()).isEqualTo(0);
        assertThat(summary.getLectureCountUntilToday()).isEqualTo(2); // 오늘과 어제 강의 포함
        assertThat(summary.getTotalLectureCount()).isEqualTo(3);
    }

    @Test
    @DisplayName("기존 출석 통계가 조퇴 업데이트될 때 정상적으로 반영된다.")
    void handleAttendanceUpdatedToEarlyLeave() {
        //given
        Attendance initialAttendance = Attendance.builder()
                .student(student)
                .lecture(todayLecture)
                .attendanceStatus(AttendanceStatus.ATTENDANCE)
                .build();
        Attendance savedInitialAttendance = attendanceRepository.save(initialAttendance);

        TestTransaction.flagForCommit();
        TestTransaction.end();
        TestTransaction.start();

        AttendanceUpdatedEvent event = new AttendanceUpdatedEvent(savedInitialAttendance, null, initialAttendance.getAttendanceStatus());
        attendanceSummaryService.handleAttendanceUpdatedEvent(event);

        TestTransaction.flagForCommit();
        TestTransaction.end();
        TestTransaction.start();

        //when
        AttendanceUpdatedEvent updateEvent = new AttendanceUpdatedEvent(savedInitialAttendance, savedInitialAttendance.getAttendanceStatus(), AttendanceStatus.EARLY_LEAVE);
        attendanceSummaryService.handleAttendanceUpdatedEvent(updateEvent);


        //then
        AttendanceSummary summary = attendanceSummaryRepository.findById(new AttendanceSummaryId(student.getId(), course.getId())).orElse(null);

        assertThat(summary).isNotNull();
        assertThat(summary.getAttendanceCountUntilToday()).isEqualTo(0);
        assertThat(summary.getEarlyLeaveCountUntilToday()).isEqualTo(1);
        assertThat(summary.getLectureCountUntilToday()).isEqualTo(2);
        assertThat(summary.getAbsenceCountUntilToday()).isEqualTo(0);
        assertThat(summary.getTotalLectureCount()).isEqualTo(2);

    }

    @Test
    @DisplayName("지각 횟수가 누적되어 조정된 결석 횟수가 정상적으로 반영된다.")
    void handleAdjustedAbsenceByLatesAndEarlyLeaves() {
        //given, when
        Attendance firstLateAttendance = Attendance.builder()
                .student(student)
                .lecture(todayLecture)
                .attendanceStatus(AttendanceStatus.LATE)
                .build();
        Attendance savedFirstLate = attendanceRepository.save(firstLateAttendance);

        TestTransaction.flagForCommit();
        TestTransaction.end();
        TestTransaction.start();
        attendanceSummaryService.handleAttendanceUpdatedEvent(new AttendanceUpdatedEvent(savedFirstLate, null, savedFirstLate.getAttendanceStatus()));
        TestTransaction.flagForCommit();
        TestTransaction.end();
        TestTransaction.start();

        Attendance secondAttendance = Attendance.builder()
                .student(student)
                .lecture(yesterdayLecture)
                .attendanceStatus(AttendanceStatus.LATE)
                .build();
        Attendance savedSecondLate = attendanceRepository.save(secondAttendance);

        TestTransaction.flagForCommit();
        TestTransaction.end();
        TestTransaction.start();
        attendanceSummaryService.handleAttendanceUpdatedEvent(new AttendanceUpdatedEvent(savedSecondLate, null, savedSecondLate.getAttendanceStatus()));
        TestTransaction.flagForCommit();
        TestTransaction.end();
        TestTransaction.start();

        Attendance thirdAttendance = Attendance.builder()
                .student(student)
                .lecture(yesterdayLecture)
                .attendanceStatus(AttendanceStatus.LATE)
                .build();
        Attendance savedThirdLate = attendanceRepository.save(thirdAttendance);

        TestTransaction.flagForCommit();
        TestTransaction.end();
        TestTransaction.start();
        attendanceSummaryService.handleAttendanceUpdatedEvent(new AttendanceUpdatedEvent(savedThirdLate, null, savedThirdLate.getAttendanceStatus()));
        TestTransaction.flagForCommit();
        TestTransaction.end();
        TestTransaction.start();

        //then
        AttendanceSummary summary = attendanceSummaryRepository.findById(new AttendanceSummaryId(student.getId(), course.getId()))
                .orElse(null);

        assertThat(summary).isNotNull();
        assertThat(summary.getLateCountUntilToday()).isEqualTo(3);
        assertThat(summary.getAdjustedAbsenceCount()).isEqualTo(1);
        assertThat(summary.getAdjustedAbsentByLateOrEarlyLeave()).isEqualTo(1);

    }
}