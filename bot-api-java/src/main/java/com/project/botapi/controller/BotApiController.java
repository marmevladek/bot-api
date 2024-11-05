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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/bot")
public class BotApiController {

    private static final Logger logger = LoggerFactory.getLogger(BotApiController.class);

    @Value("${bot.confirm_code}")
    private String botConfirmCode;

    private final BotApiService botApiService;


    public BotApiController(BotApiService botApiService) {
        this.botApiService = botApiService;
    }

    @Operation(summary = "Подтвеждение адреса сервера/Цитирование текста сообщения")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Адрес сервера подтвержден/Текст сообщения успешно процитировано", content = @Content),
            @ApiResponse(responseCode = "400", description = "Некорректный тип события", content = @Content),
            @ApiResponse(responseCode = "500", description = "Ошибка сервера", content = @Content)
    })
    @PostMapping("/webhook")
    public ResponseEntity<String> getMessage(@RequestBody Map<String, Object> body) {
        String type = (String) body.get("type");
        if (type == null) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Отсутствует \"type\"");
        try {
            if ("confirmation".equals(type)) {
                return new ResponseEntity<>(botConfirmCode, HttpStatus.OK);
            } else if ("message_new".equals(type)) {
                botApiService.sendMessage(body);
                return new ResponseEntity<>("ok", HttpStatus.OK);
            } else if ("message_reply".equals(type)) {
                return new ResponseEntity<>("ok", HttpStatus.OK);
            }

        } catch (Exception e) {
            logger.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ошибка обработки сообщения сервером");
        }

        return null;
    }
}