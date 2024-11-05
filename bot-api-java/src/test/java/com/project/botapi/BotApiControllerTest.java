package com.project.botapi;

import com.project.botapi.controller.BotApiController;
import com.project.botapi.service.BotApiService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BotApiController.class)
@ActiveProfiles("test")
class BotApiControllerTest {

    @Value("${bot.confirm_code}")
    private String confirm_code;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BotApiService botApiService;

    @Test
    void shouldReturnConfirmCode() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("type", "confirmation");

        mockMvc.perform(post("/bot/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"confirmation\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string(confirm_code));
    }

    @Test
    void shouldProcessMessageNew() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("type", "message_new");

        mockMvc.perform(post("/bot/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"message_new\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("ok"));

        Mockito.verify(botApiService).sendMessage(Mockito.any());
    }
}
