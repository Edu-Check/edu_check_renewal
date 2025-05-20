package org.example.educheck.init;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DummyDataRunner implements CommandLineRunner {

    private final DummyDataGeneratorService generatorService;

//    @Value("${app.init-dummy-data:false")
//    private boolean initDummyData;

    @Override
    public void run(String... args) throws Exception {

        generatorService.generateDummyData();
//        List<Long> campuseIds = generatorService.insertCampus();
//        List<Long> longs = generatorService.insertCourses(campuseIds);
//        generatorService.insertCourseWithCampus(campuseIds);
    }
}
