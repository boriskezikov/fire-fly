package com.example.firefly;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@Slf4j
@SpringBootApplication
@RequiredArgsConstructor
public class FireFlyApplication implements CommandLineRunner {

    private final WordsCounterService wordsCounterService;

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(FireFlyApplication.class, args);
        context.close();
        log.info("Application finished");
    }

    @Override
    public void run(String... args) {
        log.info("Start counting words...");
        wordsCounterService.countTop10Words();
    }
}
