package org.example.educheck.domain.attendance.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.educheck.domain.attendance.dto.request.AttendanceCheckinRequestDto;
import org.example.educheck.domain.attendance.dto.request.AttendanceUpdateRequestDto;
import org.example.educheck.domain.attendance.dto.response.AttendanceStatusResponseDto;
import org.example.educheck.domain.attendance.entity.Attendance;
import org.example.educheck.domain.attendance.entity.AttendanceStatus;
import org.example.educheck.domain.attendance.event.AttendanceUpdatedEvent;
import org.example.educheck.domain.attendance.repository.AttendanceRepository;
import org.example.educheck.domain.campus.Campus;
import org.example.educheck.domain.course.entity.Course;
import org.example.educheck.domain.course.entity.CourseStatus;
import org.example.educheck.domain.lecture.entity.Lecture;
import org.example.educheck.domain.lecture.repository.LectureRepository;
import org.example.educheck.domain.member.entity.Member;
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
import org.example.educheck.global.common.exception.custom.common.InvalidRequestException;
import org.example.educheck.global.common.exception.custom.common.ResourceMismatchException;
import org.example.educheck.global.common.exception.custom.common.ResourceNotFoundException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;


@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttendanceService {
    private final StudentRepository studentRepository;
    private final LectureRepository lectureRepository;
    private final AttendanceRepository attendanceRepository;
    private final MemberRepository memberRepository;
    private final StaffRepository staffRepository;
    private final StaffCourseRepository staffCourseRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public AttendanceStatusResponseDto checkIn(Member member, AttendanceCheckinRequestDto requestDto) {
        final LocalDateTime currentTime = LocalDateTime.now();
        validateStudent(member);

        Student student = studentRepository.findById(member.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("학생 정보를 찾을 수 없습니다."));

        Registration currentRegistration = student.getRegistrations().stream()
                .filter(reg -> reg.getCourse().getStatus() != CourseStatus.FINISH)
                .filter(reg -> reg.getDropDate() == null)
                .filter(reg -> reg.getCompletionDate() == null)
                .findFirst()
                .orElseThrow(() -> new ResourceMismatchException("현재 과정에 참여 중이지 않은 학생입니다."));

        Course currentCourse = currentRegistration.getCourse();
        Lecture todayLecture = lectureRepository.findByCourseIdAndDate(currentCourse.getId(), currentTime.toLocalDate())
                .orElseThrow(() -> new ResourceMismatchException("오늘 예정된 강의가 없습니다."));
        Campus campus = currentCourse.getCampus();

        if (!campus.isWithinDistance(requestDto.getLongitude(), requestDto.getLatitude())) {

            throw new InvalidRequestException("출석/퇴실 가능한 위치가 아닙니다.");
        }

        attendanceRepository.findByStudentIdTodayCheckInDate(student.getId())
                .ifPresent(attendance -> {
                    throw new AttendanceAlreadyException();
                });

        Attendance savedAttendance = attendanceRepository.save(
                Attendance.checkIn(
                        student, todayLecture, currentTime
                )
        );

        eventPublisher.publishEvent(new AttendanceUpdatedEvent(
                savedAttendance,
                null,
                savedAttendance.getAttendanceStatus()
        ));


        return new AttendanceStatusResponseDto(
                savedAttendance.getAttendanceStatus()
        );
    }

    @Transactional
    public AttendanceStatusResponseDto checkOut(Member member, AttendanceCheckinRequestDto requestDto) {
        LocalDateTime currentTime = LocalDateTime.now();
        validateStudent(member);

        Student student = studentRepository.findByMemberId(member.getId())
                .orElseThrow(() -> new ResourceNotFoundException("학생 정보를 찾을 수 없습니다."));

        Attendance attendance = attendanceRepository.findByStudentIdTodayCheckInDate(student.getId())
                .orElseThrow(() -> new ResourceNotFoundException("금일 출석 기록이 없습니다."));
        Campus campus = attendance.getLecture().getCourse().getCampus();
        if (!campus.isWithinDistance(requestDto.getLongitude(), requestDto.getLatitude())) {

            throw new InvalidRequestException("출석/퇴실 가능한 위치가 아닙니다.");
        }

        AttendanceStatus oldStatus = attendance.getAttendanceStatus();

        attendance.checkOut(currentTime);

        eventPublisher.publishEvent(new AttendanceUpdatedEvent(
                attendance,
                oldStatus,
                attendance.getAttendanceStatus()
        ));

        return new AttendanceStatusResponseDto(attendance.getAttendanceStatus());
    }

    public void updateStudentAttendance(Long courseId, Long studentId, AttendanceUpdateRequestDto requestDto, UserDetails user) {
        checkStaffHasCourse(user, courseId);


        Lecture lecture = lectureRepository.findAllByCourseId(courseId).stream()
                .filter(lec -> lec.getDate().equals(requestDto.getDate()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("해당 날짜의 강의가 없습니다."));

        Attendance attendance = attendanceRepository.findByLectureIdStudentId(studentId, lecture.getId());

        AttendanceStatus oldStatus = attendance.getAttendanceStatus();
        AttendanceStatus newStatus = requestDto.getStatus();

        attendance.setAttendanceStatus(requestDto.getStatus());
        attendanceRepository.save(attendance);

        eventPublisher.publishEvent(new AttendanceUpdatedEvent(
                attendance,
                oldStatus,
                newStatus
        ));
    }


    private void validateStudent(Member member) {
        if (!member.isStudent()) {
            throw new ForbiddenException();
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
}
