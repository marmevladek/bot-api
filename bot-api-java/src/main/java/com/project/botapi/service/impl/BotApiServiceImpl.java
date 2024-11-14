package com.project.botapi.service.impl;

import com.project.botapi.service.BotApiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.ConnectException;
import java.net.URI;
import java.util.Map;
import java.util.UUID;

@Service
public class BotApiServiceImpl implements BotApiService {

    private static final Logger logger = LoggerFactory.getLogger(BotApiServiceImpl.class);

    private final RestTemplate restTemplate;

    @Autowired
    public BotApiServiceImpl(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    @Value("${bot.token}")
    private String botToken;

    @Value("${bot.v_api}")
    private String botVApi;

    @Value("${bot.url_api}")
    private String botUrlApi;

    @Retryable(
            retryFor = {RestClientException.class, HttpClientErrorException.class, ConnectException.class},
            backoff = @Backoff(delay = 2000, multiplier = 2)
    )
    @Override
    public String sendMessage(Map<String, Object> body) {
        Map<String, Object> object;
        Map<String, Object> message;
        String text;
        Integer userId;

        try {
            object = (Map<String, Object>) body.get("object");
            if (object == null) {
                throw new IllegalArgumentException("Некорректный формат входных данных: 'object' is null");
            }

            message = (Map<String, Object>) object.get("message");
            if (message == null) {
                throw new IllegalArgumentException("Некорректный формат входных данных: 'message' is null");
            }

            text = (String) message.get("text");
            if (text == null) {
                throw new IllegalArgumentException("Некорректный формат входных данных: 'text' is null");
            }

            userId = (Integer) message.get("from_id");
            if (userId == null) {
                throw new IllegalArgumentException("Некорректный формат входных данных: 'from_id' is null");
            }

        } catch (ClassCastException | NullPointerException e) {
            logger.error("Некорректный формат входных данных: {}", e.getMessage());
            throw new IllegalArgumentException("Некорректный формат входных данных", e);
        }

        String messageText = "Вы сказали: " + text;
        int randomId = UUID.randomUUID().hashCode();

        URI url = buildMessageUri(userId, messageText, randomId);

        logger.debug("Отправка запроса на URL: {}", url);

        try {
            String response = restTemplate.postForObject(url, null, String.class);

            if (response != null && response.contains("error")) {
                logger.error("Ошибка: {}", response);
                throw new RestClientException("Ошибка: " + response);
            }

        } catch (HttpClientErrorException e) {
            logger.error("Ошибка HTTP: {}", e.getMessage());
            throw e;
        } catch (RestClientException e) {
            logger.error("Ошибка запроса: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Необработанная ошибка: {}", e.getMessage());
            throw new RuntimeException("Необработанная ошибка при отправке сообщения", e);
        }

        return "ok";
    }

    @Recover
    private String recover(HttpClientErrorException e, Map<String, Object> body) {
        logger.error("HttpClientErrorException | Не удалось отправить сообщение после нескольких попыток: {}", e.getMessage());
        return "Не удалось отправить сообщение. Пожалуйста, попробуйте позже.";
    }

    @Recover
    private String recover(RestClientException e, Map<String, Object> body) {
        logger.error("RestClientException | Не удалось отправить сообщение после нескольких попыток: {}", e.getMessage());
        return "Не удалось отправить сообщение. Пожаоуйста попробуйте позже.";
    }


    private URI buildMessageUri(Integer userId, String messageText, int randomId) {
        return UriComponentsBuilder.fromHttpUrl(botUrlApi)
                .queryParam("user_id", userId)
                .queryParam("message", messageText)
                .queryParam("random_id", randomId)
                .queryParam("access_token", "botToken")
                .queryParam("v", botVApi)
                .build()
                .toUri();
    }
}



