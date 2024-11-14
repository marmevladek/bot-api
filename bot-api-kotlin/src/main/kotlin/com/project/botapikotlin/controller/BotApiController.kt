package com.project.botapikotlin.controller

import com.project.botapikotlin.service.BotApiService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/bot")
class BotApiController(private val botApiService: BotApiService) {

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(BotApiController::class.java)
        private const val MAX_RETRY_ATTEMPTS: Int = 3
    }

    @Value("\${bot.confirm_code}")
    private val botConfirmCode: String? = null


    @Operation(summary = "Подтвеждение адреса сервера/Цитирование текста сообщения")
    @ApiResponses(
        value = [ApiResponse(
            responseCode = "200",
            description = "Адрес сервера подтвержден/Текст сообщения успешно процитировано",
            content = arrayOf(Content())
        ), ApiResponse(
            responseCode = "400",
            description = "Некорректный тип события",
            content = arrayOf(Content())
        ), ApiResponse(
            responseCode = "429",
            description = "Слишком много запросов",
            content = arrayOf(Content())
        ), ApiResponse(
            responseCode = "500",
            description = "Ошибка сервера",
            content = arrayOf(Content()))]
    )
    @PostMapping("/webhook")
    fun getMessage(
        @RequestBody body: Map<String?, Any?>,
        @RequestHeader(value = "X-Retry-Counter", required = false, defaultValue = "0") retryCounter: Int
    ): ResponseEntity<String?>? {

        try {
            if (retryCounter >= MAX_RETRY_ATTEMPTS) {
                logger.warn("Превышено максимальное количество попыток: {}. Запрос отклонен.", retryCounter)
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .header("Retry-After", "600")
                    .body("Слишком много запросов")
            }

            val type = body["type"] as String?
                ?: return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Тип события не указан")

            return when (type) {
                "confirmation" -> ResponseEntity.ok<String>(botConfirmCode)
                "message_new" -> handleMessageNew(body)
                "message_reply", "message_allow" -> ResponseEntity.ok<String>("ok")
                else -> ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body<String>("Некорректный тип события")
            }
        } catch (e: Exception) {
            logger.error("Ошибка при обработке запроса: {}", e.message, e)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Ошибка сервера")
        }
    }

     fun handleMessageNew(body: Map<String?, Any?>) : ResponseEntity<String?>? {
        try {
            return ResponseEntity.ok(botApiService.sendMessage(body))
        } catch (e: Exception) {
            logger.error("Ошибка при обработке нового сообщения", e)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Ошибка сервера при обработке сообщения")
        }
    }
}