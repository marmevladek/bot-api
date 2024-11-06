package com.project.botapi.service.impl;

import com.project.botapi.service.BotApiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
public class BotApiServiceImpl implements BotApiService {

    private static final Logger logger = LoggerFactory.getLogger(BotApiServiceImpl.class);

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${bot.token}")
    private String botToken;

    @Value("${bot.v_api}")
    private String botVApi;

    @Value("${bot.url_api}")
    private String botUrlApi;

    @Override
    public void sendMessage(Map<String, Object> body) {
        try {
            Map<String, Object> object = (Map<String, Object>) body.get("object");
            Map<String, Object> message = (Map<String, Object>) object.get("message");


            String text = (String) message.get("text");
            Integer userId = (Integer) message.get("from_id");

            if (userId != null && text != null) {
                int randomId = (int) (System.currentTimeMillis() & 0xfffffff);
                String url = String.format("%s?user_id=%s&message=%s&random_id=%d&access_token=%s&v=%s",
                        botUrlApi,
                        URLEncoder.encode(String.valueOf(userId), StandardCharsets.UTF_8),
                        "Вы сказали: " + text,
                        randomId,
                        URLEncoder.encode(botToken, StandardCharsets.UTF_8),
                        URLEncoder.encode(botVApi, StandardCharsets.UTF_8)
                );

                String response = restTemplate.postForObject(url, null, String.class);
                logger.info("Response from Bot API: {}", response);

            }
        } catch (Exception e) {
            logger.error("Не удалось отправить сообщение", e);
        }
    }
}
