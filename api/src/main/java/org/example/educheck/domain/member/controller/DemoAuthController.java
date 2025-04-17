package org.example.educheck.domain.member.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.educheck.domain.member.dto.LoginRequestDto;
import org.example.educheck.domain.member.dto.LoginResponseDto;
import org.example.educheck.domain.member.service.AuthService;
import org.example.educheck.global.common.dto.ApiResponse;
import org.example.educheck.global.common.exception.custom.LoginValidationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class DemoAuthController {

    private final AuthService authService;

    @Value("${DEMO_STUDENT_EMAIL}")
    private String demoStudentEmail;

    @Value("${DEMO_STUDENT_PASSWORD}")
    private String demoStudentPassword;

    @Value("${DEMO_MIDDLE_ADMIN_EMAIL}")
    private String demoMiddleAdminEmail;

    @Value("${DEMO_MIDDLE_ADMIN_PASSWORD}")
    private String demoMiddleAdminPassword;

    @PostMapping("/demo-student-login")
    public ResponseEntity<ApiResponse<LoginResponseDto>> loginAsDemoStudent(HttpServletResponse response) {

        LoginRequestDto demoLoginRequestDto = LoginRequestDto.createDemoLoginRequest(demoStudentEmail, demoStudentPassword);
        try {
            return ResponseEntity.ok(
                    ApiResponse.ok("로그인 성공", "OK",
                            authService.login(demoLoginRequestDto, response))

            );
        } catch (BadCredentialsException ex) {
            throw new LoginValidationException();
        }
    }

    @PostMapping("/demo-middle-admin-login")
    public ResponseEntity<ApiResponse<LoginResponseDto>> loginAsDemoMiddleAdmin(HttpServletResponse response) {

        LoginRequestDto demoLoginRequestDto = LoginRequestDto.createDemoLoginRequest(demoMiddleAdminEmail, demoMiddleAdminPassword);
        try {
            return ResponseEntity.ok(
                    ApiResponse.ok("로그인 성공", "OK",
                            authService.login(demoLoginRequestDto, response))

            );
        } catch (BadCredentialsException ex) {
            throw new LoginValidationException();
        }
    }


}
