package org.example.educheck.domain.attendanceRegister.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.educheck.domain.attendanceRegister.dto.response.adminStudentDetail.AttendanceRateProjection;
import org.example.educheck.domain.attendanceRegister.dto.response.adminStudentDetail.AttendanceRecordResponseDto;
import org.example.educheck.domain.attendanceRegister.dto.response.adminStudentDetail.StudentAttendanceOverviewDto;
import org.example.educheck.domain.attendanceRegister.dto.response.myAttendanceRecord.MyAttendanceRecordProjection;
import org.example.educheck.domain.attendanceRegister.dto.response.myAttendanceRecord.MyAttendanceRecordResponseDto;
import org.example.educheck.domain.attendanceRegister.dto.response.myAttendanceStatics.MyAttendanceStaticsProjection;
import org.example.educheck.domain.attendanceRegister.dto.response.myAttendanceStatics.MyAttendanceStaticsResponseDto;
import org.example.educheck.domain.attendanceRegister.dto.response.today.TodayLectureAttendanceResponseDto;
import org.example.educheck.domain.attendanceRegister.dto.response.today.TodayLectureAttendanceStatus;
import org.example.educheck.domain.attendanceRegister.entity.AttendanceRegister;
import org.example.educheck.domain.attendanceRegister.repository.AttendanceRegisterRepository;
import org.example.educheck.domain.course.entity.Course;
import org.example.educheck.domain.course.service.CourseService;
import org.example.educheck.domain.member.entity.Member;
import org.example.educheck.domain.member.student.entity.Student;
import org.example.educheck.domain.member.student.service.StudentService;
import org.example.educheck.domain.staffcourse.repository.StaffCourseRepository;
import org.example.educheck.global.common.exception.custom.common.ForbiddenException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AttendanceRegisterService {

    private final AttendanceRegisterRepository attendanceRegisterRepository;
    private final StaffCourseRepository staffCourseRepository;
    private final StudentService studentService;
    private final CourseService courseService;


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

    public MyAttendanceStaticsResponseDto getAttendanceDashboardData(Member member, Long courseId) {

        Course validCourse = courseService.getValidCourse(courseId);
        Student enrolledStudent = studentService.getEnrolledStudent(member.getStudentId(), courseId);
        MyAttendanceStaticsProjection projection = attendanceRegisterRepository.findAttendanceSummaryByStudentIdAndCourseId(enrolledStudent.getId(), courseId);

        return MyAttendanceStaticsResponseDto.from(projection, validCourse);
    }

    public MyAttendanceRecordResponseDto getMonthlyAttendance(Member member, Long courseId, Integer year, Integer month) {
        LocalDate start = YearMonth.of(year, month).atDay(1);
        LocalDate end = YearMonth.of(year, month).atEndOfMonth();
        Long studentId = member.getStudentId();

        Student enrolledStudent = studentService.getEnrolledStudent(studentId, courseId);
        List<MyAttendanceRecordProjection> projection = attendanceRegisterRepository.findByStudentIdAndCourseIdAndDateYearAndDateMonth(studentId, courseId, start, end);
        return MyAttendanceRecordResponseDto.from(member.getStudent().getId(), courseId, projection);
    }
}
