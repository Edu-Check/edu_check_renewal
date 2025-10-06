package org.example.educheck.global.fcm;

import lombok.RequiredArgsConstructor;
import org.example.educheck.global.fcm.service.FCMService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/fcm")
@RequiredArgsConstructor
public class FCMTestController {

    private final FCMService fcmService;

    @PostMapping("/test")
    public ResponseEntity<String> test(@RequestParam String token) {
        if (token == null || token.isEmpty()) {
            return ResponseEntity.badRequest().body("FCM 토큰이 필요합니다둥");
        }

        fcmService.sendNotification(List.of(token), "테스트 알림 타이틀", "테스트 알림 내용"
        ,999L, 2L);

        return ResponseEntity.ok("테스트 알림을 다음 토큰으로 전송했습니다. : " + token);
    }
}
