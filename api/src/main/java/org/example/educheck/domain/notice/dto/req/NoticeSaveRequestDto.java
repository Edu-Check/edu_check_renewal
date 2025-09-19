package org.example.educheck.domain.notice.dto.req;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class NoticeSaveRequestDto {

    private Long courseId;
    private String content;
}
