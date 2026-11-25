package com.vyuhstack.example;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;

@RestController
public class HelloController {

    // The commit this process was started from, so each deploy is visible.
    private static final String COMMIT = commit()
    private static final String STARTED_AT = Instant.now().toString();

    @GetMapping(value = "/", produces = MediaType.TEXT_HTML_VALUE)
    public String index() {
        return "<h1>Hello from VyuhStack</h1>"
                + "<p>A Spring Boot app deployed on every push.</p>"
                + "<p>Commit <code>" + COMMIT + "</code>, started <code>" + STARTED_AT + "</code></p>";
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "ok", "commit", COMMIT, "startedAt", STARTED_AT);
    }

    private static String commit() {
        try {
            Process process = new ProcessBuilder("git", "rev-parse", "--short", "HEAD")
                    .redirectErrorStream(true)
                    .start();
            String out = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();
            return process.waitFor() == 0 ? out : "unknown";
        } catch (IOException e) {
            return "unknown";
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "unknown";
        }
    }
}
