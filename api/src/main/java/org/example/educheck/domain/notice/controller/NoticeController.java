package org.example.educheck.domain.notice.controller;

import lombok.RequiredArgsConstructor;
import org.example.educheck.domain.member.entity.Member;
import org.example.educheck.domain.notice.dto.SendNoticeRequestDto;
import org.example.educheck.domain.notice.service.NoticeService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;


    @PostMapping("/{courseId}/notices")
    public ResponseEntity<Object> sendNoticeToCourse(@PathVariable Long courseId, @RequestBody SendNoticeRequestDto requestDto,  @AuthenticationPrincipal Member member) {
        noticeService.sendCourseNotice(courseId, requestDto, member.getId());

        return ResponseEntity.accepted().build();
    }
}
