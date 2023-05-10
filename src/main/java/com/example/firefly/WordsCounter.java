package com.example.firefly;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class WordsCounter {

    private final ArticlesCache articlesCache;
    private final WordBankCache wordBankCache;
    private final WebClient webClient;

    private final ConcurrentHashMap<String, Integer> wordCountMap = new ConcurrentHashMap<>();

    @PostConstruct
    @SneakyThrows
    public void countTop10Words() {
        long start = System.currentTimeMillis();

        while (articlesCache.getSize() > 0) {
            List<String> batch = articlesCache.getBatch(50);

            Flux.fromIterable(batch)
                    .flatMap(this::extractWords)
                    .filter(wordBankCache::isValid)
                    .doOnNext(word -> wordCountMap.compute(word, (k, v) -> v == null ? 1 : v + 1))
                    .blockLast();
        }

        printTop10Words();
        long end = System.currentTimeMillis();
        log.info("Total time: {} seconds", (end - start) / 1000);
    }

    public Flux<String> extractWords(String articleUrl) {
        return webClient.get()
                .uri(articleUrl)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> Mono.empty()) // Add this line to handle 4xx errors
                .onStatus(HttpStatusCode::is5xxServerError, clientResponse -> Mono.empty()) // Add this line to handle 5xx errors
                .bodyToMono(String.class)
                .publishOn(Schedulers.parallel())
                .flatMapMany(htmlContent -> {
                    if (htmlContent == null) {
                        return Flux.empty();
                    }
                    Document doc = Jsoup.parse(htmlContent);
                    Element scriptElement = doc.select("script[type=application/ld+json]").first();
                    if(htmlContent.contains("999 Unable to process request at this time")){
                        throw new RuntimeException("Error 999");
                    }
                    if (scriptElement == null) {
                        return Flux.empty();
                    }
                    String scriptContent = scriptElement.html();

                    try {
                        ObjectMapper objectMapper = new ObjectMapper();
                        JsonNode jsonNode = objectMapper.readTree(scriptContent);
                        JsonNode articleBodyNode = jsonNode.get("articleBody");
                        if (articleBodyNode != null) {
                            String articleBody = articleBodyNode.asText();
                            List<String> words = Arrays.asList(articleBody.split("\\s+"));
                            return Flux.fromIterable(words);
                        }
                    } catch (Exception e) {
                        log.error("Error parsing JSON content from script tag", e);
                    }
                    return Flux.empty();
                })
                .doOnError(error -> log.error("Error loading article {}", error.getMessage()));
    }


    private void printTop10Words() {
        Map<String, Integer> sortedWords = wordCountMap.entrySet()
                .stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(10)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        try {
            String json = objectMapper.writeValueAsString(sortedWords);
            System.out.println("Top 10 words:");
            System.out.println(json);
        } catch (Exception e) {
            System.err.println("Failed to convert the result to JSON: " + e.getMessage());
        }
    }
}


