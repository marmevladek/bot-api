package com.project.botapi;

import com.project.botapi.service.BotApiService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class BotApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BotApiService botApiService;

    @Value("${bot.confirm_code}")
    private String botConfirmCode;

    private final String webhookUrl = "/bot/webhook";

    @Test
    void testConfirmationSuccess() throws Exception {
        mockMvc.perform(post(webhookUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\": \"confirmation\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string(botConfirmCode));
    }

    @Test
    void testMessageNewSuccess() throws Exception {
        Mockito.when(botApiService.sendMessage(Mockito.anyMap())).thenReturn("ok");

        mockMvc.perform(post(webhookUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\": \"message_new\", \"object\": {\"message\": {\"text\": \"Hello\", \"from_id\": 123}}}"))
                .andExpect(status().isOk())
                .andExpect(content().string("ok"));
    }

    @Test
    void testUnsupportedType() throws Exception {
        mockMvc.perform(post(webhookUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\": \"unknown_type\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Некорректный тип события"));
    }

    @Test
    void testRetryCounterExceeded() throws Exception {
        mockMvc.perform(post(webhookUrl)
                        .header("X-Retry-Counter", "5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\": \"message_new\"}"))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().string("Retry-After", "600"))
                .andExpect(content().string("Слишком много запросов"));
    }

    @Test
    void testInternalServerError() throws Exception {
        Mockito.when(botApiService.sendMessage(Mockito.anyMap())).thenThrow(new RuntimeException("Test exception"));

        mockMvc.perform(post(webhookUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\": \"message_new\"}"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Ошибка сервера при обработке сообщения"));
    }

}