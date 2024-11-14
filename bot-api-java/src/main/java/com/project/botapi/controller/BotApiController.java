package com.project.botapi.controller;

import com.project.botapi.service.BotApiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/bot")
public class BotApiController {

    private static final Logger logger = LoggerFactory.getLogger(BotApiController.class);
    private static final int MAX_RETRY_ATTEMPTS = 3;

    @Value("${bot.confirm_code}")
    private String botConfirmCode;


    private final BotApiService botApiService;

    public BotApiController(BotApiService botApiService) {
        this.botApiService = botApiService;
    }


    @Operation(summary = "Подтверждение адреса сервера/Цитирование текста сообщения")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Адрес сервера подтвержден/Текст сообщения успешно процитирован",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Некорректный тип события",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Ошибка сервера",
                    content = @Content),
            @ApiResponse(
                    responseCode = "429",
                    description = "Слишком много запросов",
                    content = @Content)
    })
    @PostMapping("/webhook")
    public ResponseEntity<String> getMessage(
            @RequestBody Map<String, Object> body,
            @RequestHeader(value = "X-Retry-Counter", required = false, defaultValue = "0") int retryCounter) {
        try {
            if (retryCounter >= MAX_RETRY_ATTEMPTS) {
                logger.warn("Превышено максимальное количество попыток: {}. Запрос отклонен.", retryCounter);
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                        .header("Retry-After", "600")
                        .body("Слишком много запросов");
            }

            String type = (String) body.get("type");
            if (type == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Тип события не указан");
            }

            return switch (type) {
                case "confirmation" -> ResponseEntity.ok(botConfirmCode);
                case "message_new" -> handleMessageNew(body);
                case "message_reply", "message_allow" -> ResponseEntity.ok("ok");
                default -> ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Некорректный тип события");
            };
        } catch (Exception e) {
            logger.error("Ошибка при обработке запроса: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Ошибка сервера");
        }
    }

    private ResponseEntity<String> handleMessageNew(Map<String, Object> body) {
        try {
            return ResponseEntity.ok(botApiService.sendMessage(body));
        } catch (Exception e) {
            logger.error("Ошибка при обработке нового сообщения", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Ошибка сервера при обработке сообщения");
        }
    }
}
