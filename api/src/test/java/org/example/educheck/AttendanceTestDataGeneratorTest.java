package org.example.educheck.init;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;


@SpringBootTest
@ActiveProfiles("test")
class AttendanceTestDataGeneratorTest {

    @Autowired
    private AttendanceTestDataGenerator dataGenerator;

    @Test
    void 데이터_생성_테스트() {
        dataGenerator.generateTestData();
    }



}