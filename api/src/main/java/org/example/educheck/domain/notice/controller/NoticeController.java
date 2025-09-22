package org.example.educheck.domain.notice.controller;

import lombok.RequiredArgsConstructor;
import org.example.educheck.domain.notice.dto.SendNoticeRequestDto;
import org.example.educheck.domain.notice.service.NoticeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;


    @PostMapping("/{courseId}/notices")
    public ResponseEntity<Object> sendNoticeToCourse(@PathVariable Long courseId, @RequestBody SendNoticeRequestDto requestDto) {
        noticeService.sendCourseNotice(courseId, requestDto);

        return ResponseEntity.accepted().build();
    }
}
