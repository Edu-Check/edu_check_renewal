package org.example.educheck.domain.attendanceRegister.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.educheck.domain.attendanceRegister.dto.response.today.TodayLectureAttendanceResponseDto;
import org.example.educheck.domain.attendanceRegister.dto.response.today.TodayLectureAttendanceStatus;
import org.example.educheck.domain.attendanceRegister.entity.AttendanceRegister;
import org.example.educheck.domain.attendanceRegister.repository.AttendanceRegisterRepository;
import org.example.educheck.domain.member.entity.Member;
import org.example.educheck.domain.member.student.entity.Student;
import org.example.educheck.domain.member.student.repository.StudentRepository;
import org.example.educheck.domain.member.student.service.StudentService;
import org.example.educheck.domain.registration.repository.RegistrationRepository;
import org.example.educheck.domain.staffcourse.repository.StaffCourseRepository;
import org.example.educheck.global.common.exception.custom.common.ForbiddenException;
import org.example.educheck.global.common.exception.custom.common.ResourceNotFoundException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class AttendanceRegisterService {

    private final AttendanceRegisterRepository attendanceRegisterRepository;
    private final StaffCourseRepository staffCourseRepository;
    private final StudentRepository studentRepository;
    private final RegistrationRepository registrationRepository;
    private final StudentService studentService;

    public TodayLectureAttendanceResponseDto getTodayLectureAttendances(Long courseId, Member member) {
        validateStaffAuthorizationInCourse(member, courseId);
        List<AttendanceRegister> todayAttendances = attendanceRegisterRepository.findByCourseIdAndLectureDateIsToday(courseId);

        List<TodayLectureAttendanceStatus> attendanceStatusList = todayAttendances.stream()
                .map(TodayLectureAttendanceStatus::from)
                .toList();

        return TodayLectureAttendanceResponseDto.from(member.getId(), attendanceStatusList);
    }

    private void validateStaffAuthorizationInCourse(Member member, Long courseId) {
        if(!member.canAccessCourse(courseId, staffCourseRepository)) {
            throw new ForbiddenException("출석부 조회는 해당 과정의 관리자만 조회 가능합니다.");
        }
    }

    public void getStudentAttendanceRecordLists(Member member, Long studentId, Long courseId, Pageable pageable) {
        member.validateStaffAccessToCourse(courseId, staffCourseRepository);
        Student enrolledStudent = studentService.getEnrolledStudent(studentId, courseId);


    }
}
