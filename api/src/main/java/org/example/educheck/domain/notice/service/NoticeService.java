package org.example.educheck.domain.notice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.educheck.global.common.exception.custom.common.InvalidRequestException;
import org.example.educheck.global.rabbitmq.dto.NoticeMessageDto;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoticeService {

    @Value("${rabbitmq.exchange.name}")
    private String exchangeName;

    @Value("${rabbitmq.routing-key.prefix}")
    private String routingKeyPrefix;

    private final RabbitTemplate rabbitTemplate;

    public void sendCourseNotice(String courseName, String message) {
        validateInputs(courseName, message);
        String safeCourseName = sanitizeCourseName(courseName);
        String routingKey = getRoutingKey(safeCourseName);

        NoticeMessageDto noticeMessage = createNoticeMessage(courseName, message);
        log.info("Sending course notice message to exchange : {}, routing key: {}, message: {}", exchangeName, routingKey, noticeMessage);

        rabbitTemplate.convertAndSend(exchangeName, routingKey, noticeMessage);
    }

    private static NoticeMessageDto createNoticeMessage(String courseName, String message) {
        return NoticeMessageDto.from(courseName, message);
    }

    private void validateInputs(String courseName, String message) {
        if (courseName == null || courseName.isBlank()) {
            throw new InvalidRequestException("교육과정명은 필수입니다.");
        }
        if (message == null || message.isBlank()) {
            throw new InvalidRequestException("공지사항 내용은 필수입니다.");
        }
    }

    private String getRoutingKey(String courseName) {
        return routingKeyPrefix + courseName;
    }

    private static String sanitizeCourseName(String raw) {
        String cleaned = raw.replace(".", "_")
                .replace("#", "")
                .replace("*", "");
        return cleaned.replaceAll("[^A-Za-z0-9_-]", "_"); // 알파벳, 숫자, 언더바, 하이픈 외 모든 문자는 언더바로 변경
    }
}
