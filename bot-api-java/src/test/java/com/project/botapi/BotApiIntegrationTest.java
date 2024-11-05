package com.project.botapi;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class BotApiIntegrationTest {

    @Value("${bot.confirm_code}")
    private String confirm_code;

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void shouldReturnConfirmationCode() {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("type", "confirmation");

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                "http://localhost:" + port + "/bot/webhook", request, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(confirm_code, response.getBody());
    }

    @Test
    void shouldProcessNewMessage() {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("type", "message_new");
        requestBody.put("object", Map.of("message", Map.of("from_id", 123, "text", "Hello")));

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                "http://localhost:" + port + "/bot/webhook", request, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("ok", response.getBody());
    }
}
