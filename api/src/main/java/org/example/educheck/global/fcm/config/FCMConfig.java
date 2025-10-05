package org.example.educheck.global.fcm.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Configuration
public class FCMConfig {

    @Value("${fcm.service-account-file}")
    private Resource serviceAccountFile;

    @PostConstruct
    public void initialize() {
        try {
            InputStream serviceAccount = serviceAccountFile.getInputStream();

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            //FirebaseApp이 아직 초기화되지 않았다면 초기화를 진행
            if(FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                log.info("FirebaseApp 초기화 성공 via file : {}", serviceAccountFile.getFilename());
            }
        } catch (IOException e) {
            log.error("Firebase App 초기화 실패 resource : {}, Error : {}", serviceAccountFile.getFilename(), e.getMessage());
        }


    }
}
