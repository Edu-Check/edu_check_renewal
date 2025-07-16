package org.example.educheck.domain.absenceattendanceattachmentfile.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.educheck.domain.absenceattendanceattachmentfile.entity.AbsenceAttendanceAttachmentFile;


@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AttachmentFileResponseDto {

    private Long fileId;
    private String originalName;
    private String fileUrl;
    private String s3Key;
    private String mine;

    public static AttachmentFileResponseDto from(AbsenceAttendanceAttachmentFile file, String accessUrl) {
        return AttachmentFileResponseDto.builder()
                .fileId(file.getId())
                .originalName(file.getOriginalName())
                .fileUrl(accessUrl)
                .mine(file.getMime())
                .build();

    }

}
