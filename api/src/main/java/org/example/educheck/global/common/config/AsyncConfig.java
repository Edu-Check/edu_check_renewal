package org.example.educheck.global.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableRetry
@Configuration
@EnableAsync
public class AsyncConfig {
}
