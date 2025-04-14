package org.example.educheck.global.common.dto;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

@Getter
@Builder
public class PageInfo {

    private int pageNumber;
    private int pageSize;
    private int totalPages;
    private long totalElement;
    private boolean first;
    private boolean last;

    public static PageInfo from(Page<?> page) {
        return PageInfo.builder()
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalPages(page.getTotalPages())
                .totalElement(page.getTotalElements())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }
}
