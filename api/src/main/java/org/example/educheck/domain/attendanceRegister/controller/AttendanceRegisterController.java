package org.example.educheck.domain.attendanceRegister.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.educheck.domain.attendance.service.AttendanceSummaryService;
import org.example.educheck.domain.attendanceRegister.dto.response.adminStudentDetail.StudentAttendanceOverviewDto;
import org.example.educheck.domain.attendanceRegister.dto.response.myAttendanceRecord.MyAttendanceRecordResponseDto;
import org.example.educheck.domain.attendanceRegister.dto.response.myAttendanceStatics.MyAttendanceStaticsResponseDtoV1;
import org.example.educheck.domain.attendanceRegister.dto.response.today.TodayLectureAttendanceResponseDto;
import org.example.educheck.domain.attendanceRegister.service.AttendanceRegisterService;
import org.example.educheck.domain.course.repository.CourseRepository;
import org.example.educheck.domain.member.entity.Member;
import org.example.educheck.global.common.dto.ApiResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AttendanceRegisterController {

    private final AttendanceRegisterService attendanceRegisterService;
    private final CourseRepository courseRepository;
    private final AttendanceSummaryService attendanceSummaryService;

    @PreAuthorize("hasAuthority('MIDDLE_ADMIN')")
    @GetMapping("/courses/{courseId}/attendances/today")
    public ResponseEntity<ApiResponse<TodayLectureAttendanceResponseDto>> getTodayLectureAttendances(
            @PathVariable Long courseId,
            @AuthenticationPrincipal Member member
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                "금일 출석 현황 조회 성공",
                "OK",
                attendanceRegisterService.getTodayLectureAttendances(courseId, member)
        ));
    }

    @PreAuthorize("hasAnyAuthority('MIDDLE_ADMIN')")
    @GetMapping("/courses/{courseId}/students/{studentId}/attendances")
    public ResponseEntity<ApiResponse<StudentAttendanceOverviewDto>>
    getStudentAttendances(
            @AuthenticationPrincipal Member member,
            @PathVariable Long courseId,
            @PathVariable Long studentId,
            @PageableDefault(sort = {"lectureDate", "lectureSession"},
            direction = Sort.Direction.ASC,
            size = 8
    ) Pageable pageable)
     {
        return ResponseEntity.ok(ApiResponse.ok(
                "수강생 세부 출결 현황 조회 성공",
                "OK",
                attendanceRegisterService.getStudentAttendanceRecordLists(member, studentId, courseId, pageable)
        ));
    }

    @PreAuthorize("hasAnyAuthority('STUDENT')")
    @GetMapping("old-v/my/course/{courseId}/attendances/stats")
    public ResponseEntity<ApiResponse<MyAttendanceStaticsResponseDtoV1>> getAttendancesStats(@AuthenticationPrincipal Member member,
                                                                                           @PathVariable Long courseId) {

        return ResponseEntity.ok(
                ApiResponse.ok(
                        "내 출석률 및 결석 현황 조회 성공",
                        "OK",
                        attendanceRegisterService.getAttendanceDashboardData(member, courseId)
                )
        );
    }



    @PreAuthorize("hasAuthority('STUDENT')")
    @GetMapping("/my/courses/{courseId}/attendances")
    public ResponseEntity<ApiResponse<MyAttendanceRecordResponseDto>> getAttendances(@AuthenticationPrincipal Member member,
                                                                                     @PathVariable Long courseId,
                                                                                     @RequestParam(required = true) Integer year,
                                                                                     @RequestParam(required = true) Integer month
    ) {

        return ResponseEntity.ok(ApiResponse.ok(
                "월별 출석부 조회 성공",
                "OK",
                attendanceRegisterService.getMonthlyAttendance(member, courseId, year, month)

        ));
    }

}
