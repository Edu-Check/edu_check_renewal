package org.example.educheck.global.fcm.service;

import com.google.firebase.messaging.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.educheck.domain.member.entity.FcmToken;
import org.example.educheck.domain.member.entity.Member;
import org.example.educheck.domain.member.repository.FcmTokenRepository;
import org.example.educheck.domain.notice.entity.Notice;
import org.example.educheck.domain.registration.repository.RegistrationRepository;
import org.example.educheck.global.common.exception.ErrorCode;
import org.example.educheck.global.common.exception.custom.common.GlobalException;
import org.example.educheck.global.rabbitmq.dto.NoticeMessageDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.View;

import java.util.List;
import java.util.Map;

@Slf4j
@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class FCMService {

    private final FcmTokenRepository fcmTokenRepository;
    private final RegistrationRepository registrationRepository;
    private final View error;

    @Transactional
    public void registerFcmToken(Member member, String token) {

        if (member == null) {
            log.warn("member가 null이라 FCM Token을 등록할 수 없습니다. Token : {}", token );
            fcmTokenRepository.findByToken(token).ifPresent(fcmTokenRepository::delete);
            return;
        }

        fcmTokenRepository.findByToken(token)
                .ifPresentOrElse(
                        existingToken -> {
                            if (existingToken.getMember() == null || !existingToken.getMember().equals(member)) {
                                existingToken.updateMember(member);
                            }
                        },
                        () -> {
                            FcmToken newFcmToken = FcmToken.create(member, token);
                            fcmTokenRepository.save(newFcmToken);
                        }
                );

    }

    /**
     * 특정 강의를 수강하는 모든 학생에게 알림을 보낸다.
     */
    public void sendNotificationToCourseStudentds(NoticeMessageDto messageDto) {
        Long courseId = messageDto.getCourseId();
        Long noticeId = messageDto.getNoticeId();

        List<Long> memberIds = registrationRepository.findMemberIdsByCourseId(courseId);
        if(memberIds.isEmpty()) {
            log.info("알림을 보낼 수강생이 없습니다. courseId : {}", courseId);
            return;
        }

        List<String> tokens = fcmTokenRepository.findTokensByMemberIds(memberIds);
        if (tokens.isEmpty()) {
            log.info("알림을 보낼 FCM 토큰이 없습니다.");
            return;
        }

        sendNotification(tokens, messageDto.getTitle(), messageDto.getContent(), noticeId, courseId);
    }

    /**
     * 토큰 리스트와 알림 내용을 받아, Firebase Admin SDK를 사용하여 FCM 서버로 멀티캐스트(다중 전송) 요청을 보낸다. 
     * @param tokens 알림을 받을 디바이스 토큰 리스트
     * @param title 알림 제목
     * @param body 알림 본문
     */
    public void sendNotification(List<String> tokens, String title, String body, Long noticeId, Long courseId) {
        if (tokens.isEmpty()) {
            log.info("알림을 보낼 토큰(클라이언트)가 없습니다.");
            return;
        }

        String notificationTitle = (title != null) ? title : "새로운 공지";
        String notificationBody = (body != null) ? body : "새로운 공지사항이 등록되었습니다. 웹에서 확인해주세요.";


        // Data payload 구성
        Map<String, String> data = Map.of(
                "type", "NOTICE",
                "title", notificationTitle,
                "body", notificationBody,
                "noticeId", String.valueOf(noticeId),
                "courseId", String.valueOf(courseId)
        );

        // 멀티캐스트 메시지 구성
        MulticastMessage message = MulticastMessage.builder()
                .putAllData(data)
                .addAllTokens(tokens)
                .build();
        
        // FCM 서버에 메시지 전송 요청
        try {
            FirebaseMessaging.getInstance().sendEachForMulticast(message);
            log.info("FCM Notification 전송 성공, 대상 토큰 수 : {}", tokens.size());
        } catch (FirebaseMessagingException e) {
            log.error("FCM Notification 전송 실패, error : {}", e.getMessage());

            MessagingErrorCode errorCode = e.getMessagingErrorCode();

            if (errorCode == null) {
                throw new GlobalException(ErrorCode.FCM_SERVER_ERROR, e.getMessage());
            }

            switch(errorCode) {
                case UNREGISTERED:
                case INVALID_ARGUMENT:
                    throw new GlobalException(ErrorCode.INVALID_FCM_TOKEN, e.getMessage());

                case UNAVAILABLE:
                case INTERNAL:
                    throw new GlobalException(ErrorCode.FCM_SERVER_ERROR, e.getMessage());

                default:
                    log.warn("처리되지 않은 FCM 에러 코드 발생: {}. 일시적 오류로 처리", errorCode);
                    throw new GlobalException(ErrorCode.EXTERNAL_API_ERROR, e.getMessage());
            }


        }

    }
}
