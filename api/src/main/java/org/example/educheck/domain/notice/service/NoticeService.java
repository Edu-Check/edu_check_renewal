package org.example.educheck.domain.notice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.educheck.domain.course.entity.Course;
import org.example.educheck.domain.course.repository.CourseRepository;
import org.example.educheck.domain.member.entity.Member;
import org.example.educheck.domain.member.repository.MemberRepository;
import org.example.educheck.domain.notice.dto.request.NoticeMessageRequestDto;
import org.example.educheck.domain.notice.dto.response.NoticeDetailResponseDto;
import org.example.educheck.domain.notice.dto.response.NoticeListResponseDto;
import org.example.educheck.domain.notice.entity.Notice;
import org.example.educheck.domain.notice.event.NoticeCreatedEvent;
import org.example.educheck.domain.notice.port.out.NoticeEventPublisherPort;
import org.example.educheck.domain.notice.repository.NoticeRepository;
import org.example.educheck.global.common.exception.custom.common.InvalidRequestException;
import org.example.educheck.global.common.exception.custom.common.ResourceNotFoundException;
import org.example.educheck.global.rabbitmq.dto.NoticeMessageDto;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final CourseRepository courseRepository;
    private final MemberRepository memberRepository;

    private final ApplicationEventPublisher eventPublisher;

    // message를 받아서 RabbitMQ에 발행
    @Transactional
    public void sendCourseNotice(Long courseId, NoticeMessageRequestDto noticeRequestDto, Long memberId) {

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new InvalidRequestException("해당 과정이 존재하지 않습니다. id =" + courseId));

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("해당 사용자가 존재하지 않습니다. id = " + memberId));

        Notice notice = Notice.createNotice(course, member, noticeRequestDto);
        noticeRepository.save(notice);

        NoticeMessageDto messageDto = NoticeMessageDto.from(courseId, notice);
        NoticeCreatedEvent noticeCreatedEvent = NoticeCreatedEvent.create(messageDto);

        //TODO: courseId에 권한이 있는 관리자인지 확인

        eventPublisher.publishEvent(noticeCreatedEvent);
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

    public NoticeDetailResponseDto findNoticeDetail(Long courseId, Long noticeId, Member member) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("해당 과정이 존재하지 않습니다 courseId" + courseId));

        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 공지사항을 찾을 수 없습니다. id :" + noticeId));

        return NoticeDetailResponseDto.from(notice);
    }
}
