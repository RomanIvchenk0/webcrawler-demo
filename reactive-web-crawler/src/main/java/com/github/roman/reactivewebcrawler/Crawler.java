package com.github.roman.reactivewebcrawler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class Crawler {
    private static final Logger LOG = LoggerFactory.getLogger(Crawler.class);

    private final MessageDigest digest;
    private final WebClient webClient;

    public Crawler(MessageDigest digest, WebClient webClient) {
        this.digest = digest;
        this.webClient = webClient;
    }

    public void calculatePageHashes(String phpbbPrefix) {
        LOG.info("Start");
        long startTime = System.nanoTime();
        Flux.range(1, 1000)
                .map(i -> phpbbPrefix + i)
                .log()
                .flatMap(url -> webClient.get().uri(url).retrieve().bodyToMono(byte[].class).map(this::md5Str))
                .log()
                .blockLast();
        LOG.info("Finish");
        LOG.info("Elapsed time: {} ms", (System.nanoTime() - startTime) / 1_000_000);
    }

    private String md5Str(byte[] bytes) {
        digest.update(bytes);
        return Base64.getEncoder().encodeToString(digest.digest()).replace("/", "_");
    }

    public static void main(String[] args) throws NoSuchAlgorithmException {
        Crawler crawler = new Crawler(MessageDigest.getInstance("MD5"), WebClient.create());

        String prefix = "https://forum.littleone.ru/showthread.php?t=6872806&page=";
        crawler.calculatePageHashes(prefix);
    }
}
