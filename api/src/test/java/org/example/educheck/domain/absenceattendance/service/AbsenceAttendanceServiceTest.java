package org.example.educheck.domain.absenceattendance.service;

import org.example.educheck.domain.absenceattendance.dto.response.AbsenceAttendanceResponseDto;
import org.example.educheck.domain.absenceattendance.entity.AbsenceAttendance;
import org.example.educheck.domain.absenceattendance.repository.AbsenceAttendanceRepository;
import org.example.educheck.domain.absenceattendanceattachmentfile.entity.AbsenceAttendanceAttachmentFile;
import org.example.educheck.domain.absenceattendanceattachmentfile.repository.AbsenceAttendanceAttachmentFileRepository;
import org.example.educheck.domain.member.entity.Member;
import org.example.educheck.domain.member.entity.Role;
import org.example.educheck.domain.member.repository.MemberRepository;
import org.example.educheck.domain.member.student.entity.Student;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@SpringBootTest
class AbsenceAttendanceServiceTest {

    @Autowired
    private AbsenceAttendanceService absenceAttendanceService;

    @MockitoBean
    private AbsenceAttendanceRepository absenceAttendanceRepository;

    @MockitoBean
    private MemberRepository memberRepository;

    @MockitoBean
    private AbsenceAttendanceAttachmentFileRepository attendanceAttachmentFileRepository;

    @Autowired
    private CacheManager cacheManager;

    private Member mockMember;
    private Student mockStudent;
    private AbsenceAttendance mockAbsenceAttendance;

    @BeforeEach
    void setUp() {

        mockStudent = Student.builder().build();
        ReflectionTestUtils.setField(mockStudent, "id", 1L);

        // 멤버 객체 생성
        mockMember = Member.builder()
                .role(Role.STUDENT)
                .build();
        ReflectionTestUtils.setField(mockMember, "id", 1L);
        ReflectionTestUtils.setField(mockMember, "student", mockStudent);

         mockAbsenceAttendance = AbsenceAttendance.builder()
                .student(mockStudent)
                .build();
        ReflectionTestUtils.setField(mockAbsenceAttendance, "id", 1L);

        doReturn(Optional.of(mockAbsenceAttendance))
                .when(absenceAttendanceRepository)
                        .findById(anyLong());

        doReturn(Optional.of(mockMember))
                .when(memberRepository)
                .findByStudent_Id(mockStudent.getId());

        doReturn(Optional.of(mockMember))
                .when(memberRepository)
                        .findById(mockMember.getId());

        doReturn(new ArrayList<AbsenceAttendanceAttachmentFile>())
                .when(attendanceAttachmentFileRepository)
                .findByActivateFilesById(any());
        
    }

    @Test
    void testCacheHitRate() {
        clearCache();

        Long absenceAttendanceId = 1L;
        Long courseId = 1L;

        AbsenceAttendanceResponseDto firstCall = absenceAttendanceService.getAbsenceAttendance(
                mockMember, courseId, absenceAttendanceId
        );

        // 캐시 히트 예상
        AbsenceAttendanceResponseDto secondCall = absenceAttendanceService.getAbsenceAttendance(
                mockMember, courseId, absenceAttendanceId
        );

        //레포지토리 메서드 호출 횟수 검증
        verify(absenceAttendanceRepository, times(1)).findById(absenceAttendanceId);
        verify(memberRepository, times(1)).findByStudent_Id(any());
        verify(attendanceAttachmentFileRepository, times(1)).findByActivateFilesById(any());

        //결과 객체 동일성 확인
        assertThat(firstCall).isEqualToComparingFieldByField(secondCall);
    }

    private void clearCache() {
        cacheManager.getCache("absenceAttendanceCache").clear();
    }

}