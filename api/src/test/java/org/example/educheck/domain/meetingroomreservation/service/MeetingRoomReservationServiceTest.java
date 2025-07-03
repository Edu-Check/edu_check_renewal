package org.example.educheck.domain.meetingroomreservation.service;

import org.example.educheck.domain.course.entity.Course;
import org.example.educheck.domain.meetingroomreservation.dto.request.MeetingRoomReservationRequestDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

import org.example.educheck.domain.campus.Campus;
import org.example.educheck.domain.campus.repository.CampusRepository;
import org.example.educheck.domain.meetingroom.entity.MeetingRoom;
import org.example.educheck.domain.meetingroom.repository.MeetingRoomRepository;
import org.example.educheck.domain.member.entity.Member;
import org.example.educheck.domain.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.example.educheck.domain.course.repository.CourseRepository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

@SpringBootTest
class MeetingRoomReservationServiceTest {

    @Autowired
    private MeetingRoomReservationService meetingRoomReservationService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private CampusRepository campusRepository;

    @Autowired
    private MeetingRoomRepository meetingRoomRepository;

    @Autowired
    private CourseRepository courseRepository;

    private Member member;
    private Campus campus;
    private MeetingRoom meetingRoom;
    private Course course;

    @BeforeEach
    void setUp() {
        member = Member.builder()
                .name("member")
                .email("member" + System.currentTimeMillis() + "@test.com")
                .build();
        memberRepository.save(member);

        campus = Campus.builder()
                .name("Test Campus")
                .build();
        campusRepository.save(campus);

        course = Course.builder()
                .name("Test Course")
                .campus(campus)
                .build();
        courseRepository.save(course);

        meetingRoom = MeetingRoom.builder()
                .campus(campus)
                .name("Test Room")
                .build();
        meetingRoomRepository.save(meetingRoom);
    }


    @Test
    @DisplayName("동시에 100개의 예약 요청 시 1개만 성공해야 한다.")
    void 동시에_100개_예약_요청시_1개_이상_예약_성공() throws InterruptedException {
        // given
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        LocalDateTime startTime = LocalDateTime.of(2025, 7, 4, 10, 0);
        LocalDateTime endTime = LocalDateTime.of(2025, 7, 4, 11, 0);

        MeetingRoomReservationRequestDto requestDto = new MeetingRoomReservationRequestDto(startTime, endTime,meetingRoom.getId(), course.getId());

        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    meetingRoomReservationService.createReservation(member, campus.getId(), requestDto);
                    successCount.getAndIncrement();
                } catch (Exception e) {
                    failCount.getAndIncrement();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        // then
        assertEquals(10, successCount.get());
        assertEquals(90, failCount.get());
    }
}
