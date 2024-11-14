package com.project.botapi;

import com.project.botapi.service.BotApiService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class BotApiServiceTest {

    @MockBean
    private RestTemplate restTemplate;

    @Autowired
    private BotApiService botApiService;

    @Test
    void testSendMessageInvalidAccessToken() {
        Mockito.when(restTemplate.postForObject(Mockito.any(URI.class), Mockito.isNull(), Mockito.eq(String.class)))
                .thenReturn("ok");

        Map<String, Object> body = Map.of(
                "object", Map.of("message", Map.of("text", "Hello", "from_id", 123, "access_token", "2345y6"))
        );

        assertThrows(RestClientException.class, () -> botApiService.sendMessage(body));
    }

    @Test
    void testHandleInvalidBody() {
        Map<String, Object> invalidBody = Map.of();

        assertThrows(IllegalArgumentException.class, () -> botApiService.sendMessage(invalidBody));
    }

}
