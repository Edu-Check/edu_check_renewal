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
import org.example.educheck.global.common.exception.custom.common.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.ArrayList;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@SpringBootTest
class AbsenceAttendanceServiceTest {

    private static final Logger log = LoggerFactory.getLogger(AbsenceAttendanceServiceTest.class);
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
    @DisplayName("캐시 동작 여부 기본 테스트")
    void testCacheHitRate() {
        clearCache();

        Long absenceAttendanceId = 1L;
        Long courseId = 1L;

        AbsenceAttendanceResponseDto firstCall = absenceAttendanceService.findAbsenceAttendanceById(
                mockMember, courseId, absenceAttendanceId
        );

        // 캐시 히트 예상
        AbsenceAttendanceResponseDto secondCall = absenceAttendanceService.findAbsenceAttendanceById(
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

    @Test
    @DisplayName("첫 번째 조회 시 캐시 미스")
    void 첫_번쨰_조회_시_캐시_미스_성공_test() {
        clearCache();

        Long absenceAttendanceId = 1L;
        Long courseId = 1L;

        absenceAttendanceService.findAbsenceAttendanceById(
                mockMember, courseId, absenceAttendanceId
        );

        verify(absenceAttendanceRepository, times(1)).findById(absenceAttendanceId);
        verify(memberRepository, times(1)).findByStudent_Id(mockStudent.getId());

    }

    @Test
    @DisplayName("연속 조회 시 캐시 히트 확인")
    void 연속_조회_시_캐시_히트_성공_test() {
        clearCache();

        Long absenceAttendanceId = 1L;
        Long courseId = 1L;

        absenceAttendanceService.findAbsenceAttendanceById(
                mockMember, courseId, absenceAttendanceId
        );

        absenceAttendanceService.findAbsenceAttendanceById(
                mockMember, courseId, absenceAttendanceId
        );

        verify(absenceAttendanceRepository, times(1)).findById(absenceAttendanceId);
        verify(memberRepository, times(1)).findByStudent_Id(mockStudent.getId());
    }

    @Test
    @DisplayName("캐시 무효화 후 재조회 확인")
    void 캐시_무효화_성공_test() {
        clearCache();

        Long absenceAttendanceId = 1L;
        Long courseId = 1L;

        AbsenceAttendanceResponseDto firstResult = absenceAttendanceService.findAbsenceAttendanceById(
                mockMember, courseId, absenceAttendanceId
        );

        AbsenceAttendanceResponseDto cachedResult = absenceAttendanceService.findAbsenceAttendanceById(
                mockMember, courseId, absenceAttendanceId
        );

        absenceAttendanceService.cancelAttendanceAbsence(mockMember, absenceAttendanceId);

        AbsenceAttendanceResponseDto afterResult = absenceAttendanceService.findAbsenceAttendanceById(
                mockMember, courseId, absenceAttendanceId
        );

        assertThat(firstResult).isEqualTo(cachedResult);
        assertThat(afterResult).isNotEqualTo(firstResult);

    }

    @Test
    @DisplayName("캐시 최대 크기로 인한 만료")
    void 캐시_최대크기_만료_성공_test() {
        clearCache();
        for (long i = 0L; i < 130; i++) {
             mockAbsenceAttendance = AbsenceAttendance.builder()
                    .student(mockStudent)
                    .build();
             ReflectionTestUtils.setField(mockAbsenceAttendance, "id", i);

             doReturn(Optional.of(mockAbsenceAttendance))
                     .when(absenceAttendanceRepository)
                     .findById(i);

             absenceAttendanceService.findAbsenceAttendanceById(mockMember, 1L, i);
        }

        Cache cache = cacheManager.getCache("absenceAttendanceCache");

        if (cache instanceof CaffeineCache) {
            com.github.benmanes.caffeine.cache.Cache<Object, Object> nativeCache =
                    ((CaffeineCache) cache).getNativeCache();

            log.info("Cache size : {}", nativeCache.estimatedSize());
            log.info("Cache stats : {}",nativeCache.stats()) ;
        }

        assertThat(cache).isNotNull();

        if (cache instanceof CaffeineCache) {
            com.github.benmanes.caffeine.cache.Cache<Object, Object> nativeCache =
                    ((CaffeineCache) cache).getNativeCache();

            long cacheSize = nativeCache.estimatedSize();

            log.info("Cache size : {}", cacheSize);

            assertThat(cacheSize)
                    .isLessThanOrEqualTo(100);

            assertThat(cacheSize).isGreaterThan(0);
        }

    }

    @Test
    @DisplayName("존재하지 않는 유고결석 ID 조회 시 캐시 동작")
    void 존재하지_않는_유고결석_ID_조회_test() {
        clearCache();

        Long nonExistedId = 9999L;
        Long courseId = 1L;

        doReturn(Optional.empty())
                .when(absenceAttendanceRepository)
                .findById(nonExistedId);

        assertThatThrownBy(() ->
                absenceAttendanceService.findAbsenceAttendanceById(mockMember, courseId, nonExistedId))
                .isInstanceOf(ResourceNotFoundException.class);

        assertThatThrownBy(() ->
                absenceAttendanceService.findAbsenceAttendanceById(mockMember, courseId, nonExistedId))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(absenceAttendanceRepository, times(2)).findById(nonExistedId);

        Cache cache = cacheManager.getCache("absenceAttendanceCache");
        if (cache instanceof CaffeineCache) {
            com.github.benmanes.caffeine.cache.Cache<Object, Object> nativeCache =
                    ((CaffeineCache) cache).getNativeCache();
            assertThat(nativeCache.estimatedSize()).isEqualTo(0);
        }

    }

}