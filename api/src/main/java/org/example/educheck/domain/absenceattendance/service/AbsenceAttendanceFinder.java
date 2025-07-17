package org.example.educheck.domain.absenceattendance.service;

import lombok.RequiredArgsConstructor;
import org.example.educheck.domain.absenceattendance.entity.AbsenceAttendance;
import org.example.educheck.domain.absenceattendance.repository.AbsenceAttendanceRepository;
import org.example.educheck.global.common.exception.custom.common.ResourceNotFoundException;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AbsenceAttendanceFinder {

    private final AbsenceAttendanceRepository absenceAttendanceRepository;

    @Cacheable(value = "absenceAttendanceCache", key = "#absenceAttendancesId")
    public AbsenceAttendance findAbsenceAttendanceById(Long absenceAttendancesId) {
        return absenceAttendanceRepository.findById(absenceAttendancesId)
                .orElseThrow(() -> new ResourceNotFoundException("해당 유고 결석 신청 내역이 존재하지 않습니다."));
    }

}
