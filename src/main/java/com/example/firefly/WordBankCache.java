package com.example.firefly;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Component
public class WordBankCache {

    private static final String filePath = "src/main/resources/bank.txt";
    private final Set<String> wordBank;

    public WordBankCache() {
        wordBank = new HashSet<>();
    }

    @PostConstruct
    public void loadFromFile() {
        Path path = Paths.get(filePath);

        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line;
            while ((line = reader.readLine()) != null) {
                String word = line.trim().toLowerCase();
                if (_isValid(word)) {
                    wordBank.add(word);
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading word bank from file: " + filePath);
            e.printStackTrace();
        }
        log.info("Word bank loaded with {} words", wordBank.size());
    }

    private boolean _isValid(String word) {
        return word.length() >= 3 && word.chars().allMatch(Character::isAlphabetic);
    }

    public boolean isValid(String word) {
        return wordBank.contains(word);
    }
}
