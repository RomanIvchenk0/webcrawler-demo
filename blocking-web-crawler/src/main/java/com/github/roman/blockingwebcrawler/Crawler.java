package com.github.roman.blockingwebcrawler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

public class Crawler {
    private static final Logger LOG = LoggerFactory.getLogger(Crawler.class);

    private final MessageDigest digest;

    private final RestTemplate restTemplate;

    public Crawler(MessageDigest digest, RestTemplate restTemplate) {
        this.digest = digest;
        this.restTemplate = restTemplate;
    }

    public void calculatePageHashes(String phpbbPrefix) {
        LOG.info("Start");
        long startTime = System.nanoTime();
        try (ExecutorService executorService = Executors.newFixedThreadPool(200)) {
            List<CompletableFuture<String>> futures = IntStream.range(1, 1000).parallel()
                    .mapToObj(i -> phpbbPrefix + i)
                    .map(url -> CompletableFuture.supplyAsync(() -> {
                        byte[] body = restTemplate.exchange(url, HttpMethod.GET, null, byte[].class).getBody();
                        String hash = md5Str(body);
                        LOG.info("Hash: {}", hash);
                        return hash;
                    }, executorService)).toList();

            CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();

            futures.stream()
                    .map(CompletableFuture::join)
                    .forEach(LOG::info);
        }

        LOG.info("Finish");
        LOG.info("Elapsed time: {} ms", (System.nanoTime() - startTime) / 1_000_000);
    }

    private String md5Str(byte[] bytes) {
        digest.update(bytes);
        return Base64.getEncoder().encodeToString(digest.digest()).replace("/", "_");
    }

    public static void main(String[] args) throws NoSuchAlgorithmException {
        Crawler crawler = new Crawler(MessageDigest.getInstance("MD5"), new RestTemplate());

        String prefix = "https://forum.littleone.ru/showthread.php?t=6872806&page=";
        crawler.calculatePageHashes(prefix);
    }
}
