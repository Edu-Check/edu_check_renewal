package org.example.educheck.domain.member.dto.request;

import lombok.Getter;
import org.example.educheck.domain.member.entity.FcmToken;

@Getter
public class FcmTokenRegisterReuqestDto {

    private String fcmToken;
}
