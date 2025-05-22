package org.example.educheck;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;

import static org.junit.Assert.assertTrue;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
class ProfileCheckTest {

  @Autowired
  private Environment env;

    @Test
    void checkActiveProfile() {
        log.info("Active profiles: {}", Arrays.toString(env.getActiveProfiles()));

      assertTrue(Arrays.asList(env.getActiveProfiles()).contains("test"));
    }
}
