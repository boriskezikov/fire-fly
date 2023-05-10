package com.example.firefly;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class WordsCounter {

    private final ArticlesCache articlesCache;
    private final WordBankCache wordBankCache;
    private final RestTemplate restTemplate;

    private final ConcurrentHashMap<String, Integer> wordCountMap = new ConcurrentHashMap<>();
    private ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);
    private final AtomicInteger completedTasks = new AtomicInteger();

    @SneakyThrows
    @Async
    public void countTop10Words() {
        int totalTasks = articlesCache.getSize();
        CountDownLatch latch = new CountDownLatch(totalTasks);

        while (articlesCache.getSize() > 0) {
            String next = articlesCache.getNext();
            executor.submit(() -> {
                processArticle(next);
                latch.countDown();
                int tasksCompleted = completedTasks.incrementAndGet();
                int tasksLeft = totalTasks - tasksCompleted;
                log.info("Tasks completed: {}, tasks left: {}", tasksCompleted, tasksLeft);
            });
        }

        latch.await();
        executor.shutdown();

        printTop10Words();
    }

    private void printTop10Words() {
        Map<String, Integer> sortedWords = wordCountMap.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(10)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

        System.out.println("Top 10 words:");
        sortedWords.forEach((word, count) -> System.out.println(word + ": " + count));
    }

    public void processArticle(String articleUrl) {
        AtomicInteger total = new AtomicInteger();
        ResponseEntity<String> response = restTemplate.exchange(articleUrl, HttpMethod.GET, null, new ParameterizedTypeReference<String>() {
        });

        if (response.getStatusCode() == HttpStatus.OK) {
            String htmlContent = response.getBody();
            if (htmlContent == null) {
                return;
            }
            Document doc = Jsoup.parse(htmlContent);
            Element scriptElement = doc.select("script[type=application/ld+json]").first();
            if (scriptElement != null) {
                String scriptContent = scriptElement.html();

                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    JsonNode jsonNode = objectMapper.readTree(scriptContent);
                    JsonNode articleBodyNode = jsonNode.get("articleBody");
                    if (articleBodyNode != null) {
                        String articleBody = articleBodyNode.asText();
                        List<String> words = Arrays.asList(articleBody.split("\\s+"));

                        words.parallelStream().forEach(word -> {
                            if (wordBankCache.isValid(word)) {
                                wordCountMap.compute(word, (k, v) -> v == null ? 1 : v + 1);
                                total.addAndGet(1);
                            }
                        });
//                        log.info("Total words processed: {} for article {}", total.get(), articleUrl);
                    }
                } catch (Exception e) {
                    log.error("Error parsing JSON content from script tag", e);
                }
            }
        }
        completedTasks.incrementAndGet();
    }

    private synchronized String getNextArticle() {
        if (articlesCache.hasNext()) {
            return articlesCache.getNext();
        }
        return null;
    }
}


