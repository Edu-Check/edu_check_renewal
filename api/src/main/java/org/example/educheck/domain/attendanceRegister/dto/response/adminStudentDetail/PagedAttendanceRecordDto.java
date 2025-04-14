package org.example.educheck.domain.attendanceRegister.dto.response.adminStudentDetail;

import lombok.Builder;
import lombok.Getter;
import org.example.educheck.global.common.dto.PageInfo;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Builder
public class PagedAttendanceRecordDto {

    private List<AttendanceRecordResponseDto> attendanceRecords;
    private PageInfo pageInfo;

    public static PagedAttendanceRecordDto from(Page<AttendanceRecordResponseDto> page) {
        return PagedAttendanceRecordDto.builder()
                .attendanceRecords(page.getContent())
                .pageInfo(PageInfo.from(page))
                .build();
    }
}
