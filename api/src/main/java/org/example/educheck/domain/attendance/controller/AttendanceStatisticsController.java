package org.example.educheck.domain.attendance.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.educheck.domain.attendance.dto.response.MyAttendanceStaticsResponseDto;
import org.example.educheck.domain.attendance.service.AttendanceStatisticsService;
import org.example.educheck.domain.member.entity.Member;
import org.example.educheck.global.common.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class AttendanceStatisticsController {

    private final AttendanceStatisticsService attendanceStatisticsService;

    @PreAuthorize("hasAnyAuthority('STUDENT')")
    @GetMapping("/my/course/{courseId}/attendances/stats")
    public ResponseEntity<ApiResponse<MyAttendanceStaticsResponseDto>> getAttendancesStats(@AuthenticationPrincipal Member member,
                                                                                           @PathVariable Long courseId) {
        
        log.info("히히히");
        return ResponseEntity.ok(
                ApiResponse.ok(
                        "내 출석률 및 결석 현황 조회 성공",
                        "OK",
                        attendanceStatisticsService.getMyAttendanceStatics(member, courseId)
                )
        );
    }
}
