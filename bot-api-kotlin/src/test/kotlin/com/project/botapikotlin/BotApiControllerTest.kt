package com.project.botapikotlin

import com.project.botapikotlin.service.BotApiService
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
class BotApiControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var botApiService: BotApiService

    @Value("\${bot.confirm_code}")
    private lateinit var botConfirmCode: String

    private val webhookUrl = "/bot/webhook"

    @Test
    fun testConfirmationSuccess() {
        mockMvc.perform(post(webhookUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\": \"confirmation\"}"))
            .andExpect(status().isOk)
            .andExpect(content().string(botConfirmCode))
    }

    @Test
    fun testMessageNewSuccess() {
        Mockito.`when`(botApiService.sendMessage(Mockito.anyMap())).thenReturn("ok")

        mockMvc.perform(
            post(webhookUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"type\": \"message_new\", \"object\": {\"message\": {\"text\": \"Hello\", \"from_id\": 123}}}")
        )
            .andExpect(status().isOk())
            .andExpect(content().string("ok"))
    }

    @Test
    @Throws(Exception::class)
    fun testUnsupportedType() {
        mockMvc.perform(
            post(webhookUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"type\": \"unknown_type\"}")
        )
            .andExpect(status().isBadRequest())
            .andExpect(content().string("Некорректный тип события"))
    }

    @Test
    @Throws(Exception::class)
    fun testRetryCounterExceeded() {
        mockMvc.perform(
            post(webhookUrl)
                .header("X-Retry-Counter", "5")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"type\": \"message_new\"}")
        )
            .andExpect(status().isTooManyRequests())
            .andExpect(MockMvcResultMatchers.header().string("Retry-After", "600"))
            .andExpect(content().string("Слишком много запросов"))
    }

    @Test
    @Throws(Exception::class)
    fun testInternalServerError() {
        Mockito.`when`(botApiService.sendMessage(Mockito.anyMap())).thenThrow(RuntimeException("Test exception"))

        mockMvc.perform(
            post(webhookUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"type\": \"message_new\"}")
        )
            .andExpect(status().isInternalServerError())
            .andExpect(content().string("Ошибка сервера при обработке сообщения"))
    }
}