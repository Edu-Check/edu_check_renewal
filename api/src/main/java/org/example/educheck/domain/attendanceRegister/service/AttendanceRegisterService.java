package org.example.educheck.domain.attendanceRegister.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.educheck.domain.attendanceRegister.dto.response.AttendanceRateProjection;
import org.example.educheck.domain.attendanceRegister.dto.response.AttendanceRecordResponseDto;
import org.example.educheck.domain.attendanceRegister.dto.response.StudentAttendanceOverviewDto;
import org.example.educheck.domain.attendanceRegister.dto.response.today.TodayLectureAttendanceResponseDto;
import org.example.educheck.domain.attendanceRegister.dto.response.today.TodayLectureAttendanceStatus;
import org.example.educheck.domain.attendanceRegister.entity.AttendanceRegister;
import org.example.educheck.domain.attendanceRegister.repository.AttendanceRegisterRepository;
import org.example.educheck.domain.member.entity.Member;
import org.example.educheck.domain.member.repository.MemberRepository;
import org.example.educheck.domain.member.student.entity.Student;
import org.example.educheck.domain.member.student.service.StudentService;
import org.example.educheck.domain.staffcourse.repository.StaffCourseRepository;
import org.example.educheck.global.common.exception.custom.common.ForbiddenException;
import org.example.educheck.global.common.exception.custom.common.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AttendanceRegisterService {

    private final AttendanceRegisterRepository attendanceRegisterRepository;
    private final StaffCourseRepository staffCourseRepository;
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

    public StudentAttendanceOverviewDto getStudentAttendanceRecordLists(Member staff, Long studentId, Long courseId, Pageable pageable) {
        staff.validateStaffAccessToCourse(courseId, staffCourseRepository);
        Student enrolledStudent = studentService.getEnrolledStudent(studentId, courseId);
        Member studentMember = enrolledStudent.getMember();

        Page<AttendanceRecordResponseDto> attendanceRecord = attendanceRegisterRepository.findById_StudentIdAndId_CourseId(studentId, courseId, pageable);
        AttendanceRateProjection attendanceRate = attendanceRegisterRepository.findAttendanceStatsByStudentIdAndCourseId(studentId, courseId);

        return StudentAttendanceOverviewDto.from(studentMember, attendanceRate, attendanceRecord);

    }

    public void getAttendanceDashboardData(Member member, Long courseId) {

        Student enrolledStudent = studentService.getEnrolledStudent(member.getStudentId(), courseId);
        attendanceRegisterRepository.findAttendanceSummaryByStudentIdAndCourseId(enrolledStudent.getId(), courseId);
    }
}
