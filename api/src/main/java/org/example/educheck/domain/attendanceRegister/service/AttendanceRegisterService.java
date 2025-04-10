package org.example.educheck.domain.attendanceRegister.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.educheck.domain.attendanceRegister.dto.response.today.TodayLectureAttendanceResponseDto;
import org.example.educheck.domain.attendanceRegister.dto.response.today.TodayLectureAttendanceStatus;
import org.example.educheck.domain.attendanceRegister.entity.AttendanceRegister;
import org.example.educheck.domain.attendanceRegister.repository.AttendanceRegisterRepository;
import org.example.educheck.domain.member.entity.Member;
import org.example.educheck.domain.staffcourse.repository.StaffCourseRepository;
import org.example.educheck.global.common.exception.custom.common.ForbiddenException;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AttendanceRegisterService {

    private final AttendanceRegisterRepository attendanceRegisterRepository;
    private final StaffCourseRepository staffCourseRepository;

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
}
