package org.example.educheck.domain.attendance.service;

import lombok.RequiredArgsConstructor;
import org.example.educheck.domain.attendance.dto.response.MyAttendanceStaticsResponseDto;
import org.example.educheck.domain.attendance.entity.AttendanceSummary;
import org.example.educheck.domain.attendance.entity.AttendanceSummaryId;
import org.example.educheck.domain.attendance.repository.AttendanceSummaryRepository;
import org.example.educheck.domain.course.entity.Course;
import org.example.educheck.domain.course.service.CourseService;
import org.example.educheck.domain.member.entity.Member;
import org.example.educheck.global.common.exception.custom.common.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttendanceStatisticsService {

    private final AttendanceSummaryRepository attendanceSummaryRepository;
    private final CourseService courseService;

    public MyAttendanceStaticsResponseDto getMyAttendanceStatics(Member member, Long courseId) {

        Course validCourse = courseService.getValidCourse(courseId);

        AttendanceSummaryId summaryId = new AttendanceSummaryId(member.getStudentId(), courseId);

        //TODO: 제대로 예외 처리
        AttendanceSummary summary = attendanceSummaryRepository.findById(summaryId).orElseThrow(() -> new ResourceNotFoundException("존재하지 않습니다."));

        return MyAttendanceStaticsResponseDto.from(summary, validCourse);


    }

}
