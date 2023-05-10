package com.example.firefly;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ConcurrentLinkedQueue;

@Slf4j
@Component
public class ArticlesCache {

    private static final String filePath = "src/main/resources/articles.txt";
    private final ConcurrentLinkedQueue<String> articles;


    public ArticlesCache() {
        articles = new ConcurrentLinkedQueue<>();
    }

    @PostConstruct
    public void loadFromFile() {
        Path path = Paths.get(filePath);

        int total = 0;

        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line;
            while ((line = reader.readLine()) != null && total < 10){
                String articleUrl = line.trim().toLowerCase();
                articles.add(articleUrl);
                total++;
            }
        } catch (IOException e) {
            System.err.println("Error loading word bank from file: " + filePath);
            e.printStackTrace();
        }
        log.info("Articles loaded. Total {} stored", articles.size());
    }

    public String getNext(){
        return articles.poll();
    }

    public boolean hasNext(){
        return !articles.isEmpty();
    }

    public int getSize(){
        return articles.size();
    }

}
