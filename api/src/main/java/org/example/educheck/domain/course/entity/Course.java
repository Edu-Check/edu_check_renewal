package org.example.educheck.domain.course.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.educheck.domain.campus.Campus;
import org.example.educheck.domain.course.repository.CourseRepository;
import org.example.educheck.global.common.exception.custom.common.InvalidRequestException;

import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campus_id")
    private Campus campus;

    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "VARCHAR(10)")
    private CourseStatus status;

    @Builder
    public Course(Campus campus, String name, LocalDate startDate, LocalDate endDate, CourseStatus status) {
        this.campus = campus;
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
    }

    public void validateInProgressAt(YearMonth requestDate) {
        YearMonth startMonth = YearMonth.from(this.startDate);
        YearMonth endMonth = YearMonth.from(this.endDate);
        if (requestDate.isBefore(startMonth) || requestDate.isAfter(endMonth)) {
            throw new InvalidRequestException("해당 월은 과정 진행 중인 기간이 아닙니다.");
        }
    }

    public void validateAbsenceDateRange(LocalDate absenceStart, LocalDate absenceEnd) {
        if(absenceStart.isBefore(startDate) || absenceEnd.isAfter(endDate)) {
            throw new InvalidRequestException(String.format("결석 신청 기간은 교육 과정 기간 내여야 합니다.\n교육 기간은 %s 부터 %s입니다.", startDate, endDate));
        }
    }


}
