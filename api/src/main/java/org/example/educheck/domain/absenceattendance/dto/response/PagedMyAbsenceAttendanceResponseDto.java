package org.example.educheck.domain.absenceattendance.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.example.educheck.global.common.dto.PageInfo;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Builder
public class PagedMyAbsenceAttendanceResponseDto {

    private List<MyAbsenceAttendanceResponseDto> lists;
    private PageInfo pageInfo;

    public static PagedMyAbsenceAttendanceResponseDto from(Page<MyAbsenceAttendanceResponseDto> page) {
        return PagedMyAbsenceAttendanceResponseDto.builder()
                .lists(page.getContent())
                .pageInfo(PageInfo.from(page))
                .build();
    }
}
