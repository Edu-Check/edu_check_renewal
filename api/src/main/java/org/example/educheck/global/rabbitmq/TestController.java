package org.example.educheck.global.rabbitmq;

import lombok.RequiredArgsConstructor;
import org.example.educheck.domain.notice.service.NoticeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TestController {

    private final NoticeService noticeService;

    @GetMapping("/test/send-noticec")
    public String sendNotice(@RequestParam String course, @RequestParam String msg) {
        noticeService.sendCourseNotice(course, msg);
        return "Message snet to course: " + course;
    }
}
