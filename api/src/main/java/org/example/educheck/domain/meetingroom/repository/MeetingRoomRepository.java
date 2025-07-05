package org.example.educheck.domain.meetingroom.repository;

import org.example.educheck.domain.meetingroom.entity.MeetingRoom;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MeetingRoomRepository extends JpaRepository<MeetingRoom, Long> {

    @Query("SELECT mr FROM MeetingRoom mr JOIN FETCH mr.campus WHERE mr.id = :id")
    MeetingRoom findByIdWithCampus(@Param("id") Long id);

    @Lock(LockModeType.OPTIMISTIC_FORCE_INCREMENT)
    @Query("SELECT m FROM MeetingRoom m WHERE m.id = :id")
    Optional<MeetingRoom> findByIdWithOptimisticLock(@Param("id") Long id);
}
