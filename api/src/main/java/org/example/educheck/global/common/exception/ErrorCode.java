package org.example.educheck.global.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    //0000 회원
    EMAIL_NOT_FOUND(HttpStatus.UNAUTHORIZED, "0000", "인증 정보를 찾을 수 없습니다."),
    //1000 예약
    RESERVATION_CONFLICT(HttpStatus.CONFLICT, "1000", "해당 시간에는 이미 예약이 있습니다. 다른 시간을 선택해주세요."),

    //2000 출석
    ALREADY_ATTENDANCE(HttpStatus.BAD_REQUEST, "2000", "이미 출석 처리되었습니다."),

    //예시
    MEETINGROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "3000", "회의실을 찾을 수 없습니다."),

    //4000 공통 클라이언트 오류
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "4000", "입력값이 올바르지 않습니다."),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "4004", "해당 리소스가 존재하지 않습니다."),
    API_ENDPOINT_NOT_FOUND(HttpStatus.NOT_FOUND, "4007", "존재하지 않는 API 경로입니다."),
    MISMATCHED_RESOURCE(HttpStatus.BAD_REQUEST, "4008", "요청한 리소스가 일치하지 않습니다."),
    NOT_OWNER(HttpStatus.FORBIDDEN, "4003", "본인만 수정 및 삭제 가능합니다."),
    MAX_UPLOAD_SIZE_EXCEEDED(HttpStatus.PAYLOAD_TOO_LARGE, "4009", "파일 크기가 5MB를 초과할 수 없습니다."),

    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "4010", "인증에 실패했습니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "4030", "접근 권한이 없습니다."),

    // 5000 공통 서버 오류
    SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "5000", "서버 내부에서 오류가 발생했습니다."),
    FILE_UPLOAD_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "5001", "파일 업로드 중 오류가 발생했습니다."),
    FILE_DELETE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "5002", "파일 삭제 중 오류가 발생했습니다"),

    // 6000 알림 (Fatal Errors 재시도 불필요)
    INVALID_MESSAGE_FORMAT(HttpStatus.BAD_REQUEST, "6000", "잘못된 메시지 형식입니다."),
    INVALID_FCM_TOKEN(HttpStatus.BAD_REQUEST, "6001", "유효하지 않은 FCM 토큰입니다."),
    COURSE_NOT_FOUND(HttpStatus.NOT_FOUND, "6002", "존재하지 않는 강의입니다."),
    STUDENT_NOT_FOUND(HttpStatus.NOT_FOUND, "6003", "존재하지 않는 학생입니다."),

    //7000 알림 (Transient Errors - 재시도 필요)
    FCM_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "7000", "FCM 서버 일시적 오류가 발생했습니다."),
    NETWORK_TIMEOUT(HttpStatus.GATEWAY_TIMEOUT, "7001", "네트워크 타임아웃이 발생했습니다."),
    EXTERNAL_API_ERROR(HttpStatus.BAD_GATEWAY, "7002", "외부 API 일시적 오류가 발생했습니다.");


    private final HttpStatus status;
    private final String code;
    private final String message;

    /**
     * 4xx대 -> fatal 재시도 불필요
     * 5xx대 -> transient 재시도 필요
     */
    public boolean isFatalError() {
        return status.is4xxClientError();
    }

}
