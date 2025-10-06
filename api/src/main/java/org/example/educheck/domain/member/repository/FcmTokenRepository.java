package org.example.educheck.domain.member.repository;

import org.example.educheck.domain.member.entity.FcmToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface FcmTokenRepository extends JpaRepository<FcmToken, Long> {

    Optional<FcmToken> findByToken(String token);

    @Query("SELECT ft.token FROM FcmToken ft WHERE ft.member.id IN :memberIds")
    List<String> findTokensByMemberIds(List<Long> memberIds);
}
