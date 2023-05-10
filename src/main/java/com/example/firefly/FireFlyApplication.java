package com.example.firefly;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
@RequiredArgsConstructor
public class FireFlyApplication {


    private final WordsCounter wordsCounter;

    public static void main(String[] args) {
        SpringApplication.run(FireFlyApplication.class, args);
    }

}
