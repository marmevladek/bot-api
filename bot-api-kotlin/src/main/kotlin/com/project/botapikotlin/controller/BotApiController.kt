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
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/bot")
class BotApiController(private val botApiService: BotApiService) {

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
        ), ApiResponse(responseCode = "500", description = "Ошибка сервера", content = arrayOf(Content()))]
    )
    @PostMapping("/webhook")
    fun getMessage(@RequestBody body: Map<String?, Any?>): ResponseEntity<String>? {
        val type = body["type"] as String?
            ?: return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Отсутствует \"type\"")
        try {
            if ("confirmation" == type) {
                return ResponseEntity(botConfirmCode, HttpStatus.OK)
            } else if ("message_new" == type) {
                botApiService.sendMessage(body)
                return ResponseEntity("ok", HttpStatus.OK)
            } else if ("message_reply" == type) {
                return ResponseEntity("ok", HttpStatus.OK)
            }
        } catch (e: Exception) {
            logger.error(e.message)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ошибка обработки сообщения сервером")
        }

        return null
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(BotApiController::class.java)
    }
}