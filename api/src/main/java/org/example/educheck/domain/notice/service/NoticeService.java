package org.example.educheck.domain.notice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.educheck.domain.course.entity.Course;
import org.example.educheck.domain.course.repository.CourseRepository;
import org.example.educheck.domain.member.entity.Member;
import org.example.educheck.domain.member.repository.MemberRepository;
import org.example.educheck.domain.notice.dto.request.NoticeMessageRequestDto;
import org.example.educheck.domain.notice.dto.response.NoticeListResponseDto;
import org.example.educheck.domain.notice.entity.Notice;
import org.example.educheck.domain.notice.repository.NoticeRepository;
import org.example.educheck.global.common.exception.custom.common.InvalidRequestException;
import org.example.educheck.global.common.exception.custom.common.ResourceNotFoundException;
import org.example.educheck.global.rabbitmq.dto.NoticeMessageDto;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoticeService {

    @Value("${educheck.rabbitmq.exchange.notice}")
    private String exchangeName;

    @Value("${educheck.rabbitmq.routing-key.format.send}")
    private String routingKeyFormat;

    private final RabbitTemplate rabbitTemplate;
    private final NoticeRepository noticeRepository;
    private final CourseRepository courseRepository;
    private final MemberRepository memberRepository;

    // message를 받아서 RabbitMQ에 발행
    public void sendCourseNotice(Long courseId, NoticeMessageRequestDto noticeRequestDto, Long memberId) {

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new InvalidRequestException("해당 과정이 존재하지 않습니다. id =" + courseId));

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("해당 사용자가 존재하지 않습니다. id = " + memberId));

        Notice notice = Notice.createNotice(course, member, noticeRequestDto);
        noticeRepository.save(notice);

        NoticeMessageDto messageDto = NoticeMessageDto.from(courseId, notice);
        //TODO: courseId에 권한이 있는 관리자인지 확인
        String routingKey = getRoutingKey(courseId);

        rabbitTemplate.convertAndSend(exchangeName, routingKey, messageDto);
    }

    private String getRoutingKey(Long courseId) {
        return String.format(routingKeyFormat, courseId);
    }


    public List<NoticeListResponseDto> findAllNotices(Long courseId, Member member) {
        //TODO: courseId 유효성 검증
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("해당 과정이 존재하지 않습니다 courseId" + courseId));


        List<Notice> notices = noticeRepository.findByCourseOrderByCreatedAtDesc(course);

        return notices.stream()
                .map(NoticeListResponseDto::from)
                .collect(Collectors.toList());


    }
}
