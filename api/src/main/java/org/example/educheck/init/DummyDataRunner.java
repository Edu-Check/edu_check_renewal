package org.example.educheck.init;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DummyDataRunner implements CommandLineRunner {

    private final DummyDataGeneratorService generatorService;

//    @Value("${app.init-dummy-data:false")
//    private boolean initDummyData;

    @Override
    public void run(String... args) throws Exception {
//        if (!initDummyData) {
//            return;
//        }

        generatorService.generateDummyData();
    }
}
