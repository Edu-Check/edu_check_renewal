package org.example.educheck.domain.notice.controller;

import lombok.RequiredArgsConstructor;
import org.example.educheck.domain.member.entity.Member;
import org.example.educheck.domain.notice.dto.NoticeMessageRequestDto;
import org.example.educheck.domain.notice.service.NoticeService;
import org.example.educheck.global.common.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;


    @PostMapping("/{courseId}/notices")
    public ResponseEntity<ApiResponse<Void>> sendNoticeToCourse(@PathVariable Long courseId, @RequestBody NoticeMessageRequestDto requestDto, @AuthenticationPrincipal Member member) {
        noticeService.sendCourseNotice(courseId, requestDto, member.getId());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        ApiResponse.ok(
                                "공지사항 등록 성공",
                                "OK",
                                null
                        )
                );
    }
}
