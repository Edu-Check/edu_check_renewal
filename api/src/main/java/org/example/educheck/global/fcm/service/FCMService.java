package org.example.educheck.global.fcm.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.educheck.domain.member.entity.FcmToken;
import org.example.educheck.domain.member.entity.Member;
import org.example.educheck.domain.member.repository.FcmTokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class FCMService {

    private final FcmTokenRepository fcmTokenRepository;

    @Transactional
    public void registerFcmToken(Member member, String token) {
        fcmTokenRepository.findByToken(token)
                .ifPresentOrElse(
                        existingToken -> {
                            if (!existingToken.getMember().equals(member)) {
                                existingToken.updateMember(member);
                            }
                        },
                        () -> {
                            FcmToken newFcmToken = FcmToken.create(member, token);
                            fcmTokenRepository.save(newFcmToken);
                        }
                );

    }
}
