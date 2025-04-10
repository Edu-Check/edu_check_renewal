package org.example.educheck.domain.attendance.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.educheck.domain.attendance.dto.request.AttendanceCheckinRequestDto;
import org.example.educheck.domain.attendance.dto.request.AttendanceUpdateRequestDto;
import org.example.educheck.domain.attendance.dto.response.AttendanceStatusResponseDto;
import org.example.educheck.domain.attendance.entity.Attendance;
import org.example.educheck.domain.attendance.entity.AttendanceStatus;
import org.example.educheck.domain.attendance.repository.AttendanceRepository;
import org.example.educheck.domain.campus.Campus;
import org.example.educheck.domain.course.entity.Course;
import org.example.educheck.domain.course.entity.CourseStatus;
import org.example.educheck.domain.lecture.entity.Lecture;
import org.example.educheck.domain.lecture.repository.LectureRepository;
import org.example.educheck.domain.member.entity.Member;
import org.example.educheck.domain.member.entity.Role;
import org.example.educheck.domain.member.repository.MemberRepository;
import org.example.educheck.domain.member.repository.StaffRepository;
import org.example.educheck.domain.member.staff.entity.Staff;
import org.example.educheck.domain.member.student.entity.Student;
import org.example.educheck.domain.member.student.repository.StudentRepository;
import org.example.educheck.domain.registration.entity.Registration;
import org.example.educheck.domain.registration.repository.RegistrationRepository;
import org.example.educheck.domain.staffcourse.repository.StaffCourseRepository;
import org.example.educheck.global.common.exception.custom.attendance.AttendanceAlreadyException;
import org.example.educheck.global.common.exception.custom.common.ForbiddenException;
import org.example.educheck.global.common.exception.custom.common.ResourceNotFoundException;
import org.hibernate.Hibernate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;


@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttendanceService {
    private final StudentRepository studentRepository;
    private final RegistrationRepository registrationRepository;
    private final LectureRepository lectureRepository;
    private final AttendanceRepository attendanceRepository;
    private final MemberRepository memberRepository;
    private final StaffRepository staffRepository;
    private final StaffCourseRepository staffCourseRepository;

    @Transactional
    public AttendanceStatusResponseDto checkIn(Member member, AttendanceCheckinRequestDto requestDto) {
        LocalDateTime currentTime = LocalDateTime.now();
        validateStudent(member);

        Student student = studentRepository.findById(member.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("학생 정보를 찾을 수 없습니다."));

        Registration currentRegistration = student.getRegistrations().stream()
                .filter(reg -> reg.getCourse().getStatus() != CourseStatus.FINISH)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("현재 과정에 참여 중이지 않은 학생입니다."));

        Course currentCourse = currentRegistration.getCourse();
        Lecture todayLecture = lectureRepository.findByCourseIdAndDate(currentCourse.getId(), currentTime.toLocalDate())
                .orElseThrow(() -> new IllegalArgumentException("오늘 예정된 강의가 없습니다."));
        Campus campus = currentCourse.getCampus();

        validateCampusLocation(campus, requestDto.getLongitude(), requestDto.getLatitude());

        attendanceRepository.findByStudentIdTodayCheckInDate(student.getId())
                .ifPresent(attendance -> {
                    throw new AttendanceAlreadyException();
                });

        return new AttendanceStatusResponseDto(
                attendanceRepository.save(
                        Attendance.checkIn(
                                student, todayLecture, currentTime
                        )
                ).getAttendanceStatus()
        );
    }

    @Transactional
    public AttendanceStatusResponseDto checkOut(Member member, AttendanceCheckinRequestDto requestDto) {
        LocalDateTime currentTime = LocalDateTime.now();
        validateStudent(member);

        Student student = studentRepository.findByMemberId(member.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("학생 정보를 찾을 수 없습니다."));

        Attendance attendance = attendanceRepository.findByStudentIdTodayCheckInDate(student.getId())
                .orElseThrow(() -> new IllegalArgumentException("금일 출석 기록이 없습니다."));
        attendance.checkOut(currentTime);

        return new AttendanceStatusResponseDto(attendance.getAttendanceStatus());
    }

    public void updateStudentAttendance(Long courseId, Long studentId, AttendanceUpdateRequestDto requestDto, UserDetails user) {
        checkStaffHasCourse(user, courseId);


        Lecture lecture = lectureRepository.findAllByCourseId(courseId).stream()
                .filter(lec -> lec.getDate().equals(requestDto.getDate()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("해당 날짜의 강의가 없습니다."));

        Attendance attendance = attendanceRepository.findByLectureIdStudentId(studentId, lecture.getId());
        attendance.setAttendanceStatus(requestDto.getStatus());
        attendanceRepository.save(attendance);
    }


    private void validateStudent(Member member) {
        if (!member.isStudent()) {
            throw new ForbiddenException();
        }
    }


    private void validateCampusLocation(Campus campus, double longitude, double latitude) {
        if (!campus.isWithinDistance(longitude, latitude)) {
            throw new IllegalArgumentException("출석/퇴실 가능한 위치가 아닙니다.");
        }
    }


    private void checkStaffHasCourse(UserDetails user, Long courseId) {
        String email = user.getUsername();
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("해당하는 관리자가 없습니다."));
        Staff staff = staffRepository.findByMemberId(member.getId())
                .orElseThrow(() -> new ResourceNotFoundException("해당하는 관리자가 없습니다."));
        staffCourseRepository.findByStaffIdAndCourseId(staff.getId(), courseId)
                .orElseThrow(() -> new ResourceNotFoundException("관리자가 해당하는 강의를 가지고 있지 않습니다."));
    }

    private boolean isStudent(Member member) {
        return Role.STUDENT.equals(member.getRole());
    }

    @Transactional
    public AttendanceStatus checkOut(UserDetails user, AttendanceCheckinRequestDto requestDto) {

        String email = user.getUsername();
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("사용자 정보를 찾을 수 없습니다."));

        if (!isStudent(member)) {
            throw new IllegalArgumentException("학생이 아닙니다.");
        }

        Student student = studentRepository.findByMemberId(member.getId())
                .orElseThrow(() -> new ResourceNotFoundException("학생 정보를 찾을 수 없습니다."));

        Long studentId = student.getId();

        if (student.getCourseParticipationStatus() != 'T') {
            throw new IllegalArgumentException("현재 과정에 참여 중이지 않은 학생입니다.");
        }

        Registration currentRegistration = registrationRepository.findActiveRegistrationByStudentId(
                        studentId, CourseStatus.FINISH)
                .orElseThrow(() -> new IllegalArgumentException("현재 진행 중인 과정 등록이 없습니다."));
        Course currentCourse = currentRegistration.getCourse();

        LocalDate today = LocalDate.now();

        Attendance attendance = attendanceRepository.findByStudentIdTodayCheckInDate(
                        student.getId())
                .orElseThrow(() -> new IllegalArgumentException("금일 출석 기록이 없습니다."));

        Campus campus = currentCourse.getCampus();

        if (!campus.isWithinDistance(requestDto.getLongitude(), requestDto.getLatitude())) {
            throw new IllegalArgumentException("퇴실 가능한 위치가 아닙니다.");
        }

        if (attendance.getCheckOutTimestamp() != null) {
            throw new IllegalStateException("이미 퇴실 처리되었습니다.");
        }

        LocalDateTime now = LocalDateTime.now();
        attendance.setCheckOutTimestamp(now);

        Lecture todayLecture = lectureRepository.findByCourseIdAndDate(
                        currentCourse.getId(), today)
                .orElseThrow(() -> new IllegalArgumentException("오늘 예정된 강의가 없습니다."));

        LocalTime lectureEndTime = todayLecture.getEndTime();

        if (now.toLocalTime().isBefore(lectureEndTime)) {
            attendance.setAttendanceStatus(AttendanceStatus.EARLY_LEAVE);
        } else if (attendance.getAttendanceStatus() == AttendanceStatus.LATE) {
            attendance.setAttendanceStatus(AttendanceStatus.LATE);
        } else {
            attendance.setAttendanceStatus(AttendanceStatus.ATTENDANCE);
        }

        attendanceRepository.save(attendance);
        return attendance.getAttendanceStatus();
    }


}
