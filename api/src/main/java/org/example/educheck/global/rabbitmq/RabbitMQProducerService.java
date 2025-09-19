package org.example.educheck.global.rabbitmq;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.educheck.global.common.exception.custom.common.InvalidRequestException;
import org.example.educheck.global.rabbitmq.dto.NoticeMessageDto;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RabbitMQProducerService {

    private static final String EXCHANGE_NAME = "educheck.course.exchange";
    private static final String ROUTING_KEY_PREFIX = "course.";
    private static final String ROUTING_KEY_SUFFIX = ".notice";

    private final RabbitTemplate rabbitTemplate;

    public void sendCourseNotice(String courseName, String message) {
        validateInputs(courseName, message);
        String safeCourseName = senitozeCourseName(courseName);
        String routingKey = getRoutingKey(safeCourseName);

        NoticeMessageDto noticeMessage = createNoticeMessage(courseName, message);
        log.info("Sending course notice message to exchange : {}, routing key: {}, message: {}", EXCHANGE_NAME, routingKey, noticeMessage);

        rabbitTemplate.convertAndSend(EXCHANGE_NAME, routingKey, noticeMessage);
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

    private static String getRoutingKey(String courseName) {
        return ROUTING_KEY_PREFIX + courseName + ROUTING_KEY_SUFFIX;
    }

    private static String senitozeCourseName(String raw) {
        String cleaned = raw.replace(".", "_")
                .replace("#", "")
                .replace("*", "");
        return cleaned.replaceAll("[^A-Za-z0-9_-]", "_"); // 알파벳, 숫자, 언더바, 하이픈 외 모든 문자는 언더바로 변경
    }
}
