package org.example.educheck.domain.member.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.educheck.domain.member.dto.request.FcmTokenRegisterRequestDto;
import org.example.educheck.domain.member.dto.UpdateMyProfileRequestDto;
import org.example.educheck.domain.member.dto.response.MyProfileResponseDto;
import org.example.educheck.domain.member.entity.Member;
import org.example.educheck.domain.member.service.AuthService;
import org.example.educheck.domain.member.service.MyService;
import org.example.educheck.global.common.dto.ApiResponse;
import org.example.educheck.global.fcm.service.FCMService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/my")
@RequiredArgsConstructor
public class MyController {

    private static final Logger log = LoggerFactory.getLogger(MyController.class);
    private final MyService myService;
    private final AuthService authService;
    private final FCMService fcmService;

    //        @PreAuthorize("hasAuthority('STUDENT')")
    @PreAuthorize("hasAuthority('STUDENT')")
    @GetMapping
    public ResponseEntity<ApiResponse<MyProfileResponseDto>> getMyProfile(@AuthenticationPrincipal Member member) {

        log.info(member.getAuthorities().toString());
        return ResponseEntity.ok(
                ApiResponse.ok(
                        "개인 정보 조회 성공",
                        "OK",
                        myService.getMyProfile(member)
                )
        );
    }

    @PatchMapping
    public ResponseEntity<ApiResponse<Void>> updateMyProfile(
            @AuthenticationPrincipal Member member,
            @Valid @RequestBody UpdateMyProfileRequestDto requestDto,
            HttpServletRequest request,
            HttpServletResponse response) {

        myService.updateMyProfile(member, requestDto);
        authService.logout(request, response);

        return ResponseEntity.ok(
                ApiResponse.ok(
                        "개인 정보 수정 성공",
                        "OK",
                        null
                )
        );
    }

    @PostMapping("/fcm-token")
    public ResponseEntity<ApiResponse<Object>> registerFcmToken(@AuthenticationPrincipal Member member, @Valid @RequestBody FcmTokenRegisterRequestDto requestDto) {
        fcmService.registerFcmToken(member, requestDto.getFcmToken());

        return ResponseEntity.ok(
                ApiResponse.ok(
                        "FCM 토큰 저장 완료",
                        "OK",
                        null
                )
        );
    }
}
