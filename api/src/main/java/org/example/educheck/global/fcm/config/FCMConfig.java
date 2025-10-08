package org.example.educheck.global.fcm.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;

@Slf4j
@Configuration
public class FCMConfig {

    @Value("${fcm.service-account-base64}")
    private String serviceAccountBase64;

    @PostConstruct
    public void initialize() {
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(serviceAccountBase64);
            ByteArrayInputStream serviceAccount = new ByteArrayInputStream(decodedBytes);

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            //FirebaseApp이 아직 초기화되지 않았다면 초기화를 진행
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                log.info("FirebaseApp 초기화 성공 via Base64 환경변수");
            }
        } catch (IllegalStateException e) {
            log.error("Base64 디코딩 실패 : {}", e.getMessage());
        } catch (IOException e) {
            log.error("Firebase App 초기화 실패 Error : {}", e.getMessage());
        }


    }
}
