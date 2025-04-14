package org.example.educheck.domain.attendanceRegister.repository;

import org.example.educheck.domain.attendanceRegister.dto.response.myAttendanceStatics.MyAttendanceStaticsProjection;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ActiveProfiles("test")
@DataJpaTest
@Sql(scripts = "/test-data.sql")
class AttendanceRegisterRepositoryTest {

    @Autowired
    private AttendanceRegisterRepository attendanceRegisterRepository;


    @Test
    void 수강생_본인_출석률_및_결석현황_조회_성공_테스트() {
        //given
        Long studentId = 4001L;
        Long course_id = 1001L;

        //when
        MyAttendanceStaticsProjection result = attendanceRegisterRepository.findAttendanceSummaryByStudentIdAndCourseId(studentId, course_id);

        //then  (지각 2, 조퇴 1, 결석1, 조정된 결석2, 미출석2)
        // 총 강의수 6, => 1/6
        assertThat(result).isNotNull();
        assertThat(result.getLateCountUntilToday()).isEqualTo(Integer.valueOf(2));
        assertThat(result.getEarlyLateCountUntilToday()).isEqualTo(Integer.valueOf(1));
        assertThat(result.getAbsenceCountUntilToday()).isEqualTo(Integer.valueOf(1));
        assertThat(result.getAdjustedAbsenceCount()).isEqualTo(Integer.valueOf(2));
        assertThat(result.getTotalAttendanceRate()).isEqualTo(Double.valueOf(16.67));

    }
}