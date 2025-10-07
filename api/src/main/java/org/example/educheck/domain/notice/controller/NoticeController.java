package org.example.educheck.domain.notice.controller;

import lombok.RequiredArgsConstructor;
import org.example.educheck.domain.member.entity.Member;
import org.example.educheck.domain.notice.dto.request.NoticeMessageRequestDto;
import org.example.educheck.domain.notice.dto.response.NoticeDetailResponseDto;
import org.example.educheck.domain.notice.dto.response.NoticeListResponseDto;
import org.example.educheck.domain.notice.service.NoticeService;
import org.example.educheck.global.common.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping("/{courseId}/notices")
    public ResponseEntity<ApiResponse<List<NoticeListResponseDto>>> getNoticeList(@PathVariable Long courseId, @AuthenticationPrincipal Member member) {

        return ResponseEntity.ok(
                ApiResponse.ok(
                        "공지사항 목록 조회 성공",
                        "OK",
                        noticeService.findAllNotices(courseId, member)
                )
        );
    }

    @GetMapping("/{courseId}/notices/{noticeId}")
    public ResponseEntity<ApiResponse<NoticeDetailResponseDto>> getNoticeDetail(@PathVariable Long courseId, @PathVariable Long noticeId, @AuthenticationPrincipal Member member) {


        return ResponseEntity.ok(
                ApiResponse.ok(
                        "공지사항 목록 조회 성공",
                        "OK",
                        noticeService.findNoticeDetail(courseId, noticeId, member)
                )
        );
    }




}
