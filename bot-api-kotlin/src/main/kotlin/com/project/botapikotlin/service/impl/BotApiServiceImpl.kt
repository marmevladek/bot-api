package com.project.botapikotlin.service.impl

import com.project.botapikotlin.service.BotApiService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Service
class BotApiServiceImpl : BotApiService {

    private val logger: Logger = LoggerFactory.getLogger(BotApiServiceImpl::class.java)

    private val restTemplate = RestTemplate()

    @Value("\${bot.token}")
    private val botToken: String?= null;

    @Value("\${bot.v_api}")
    private val botVApi: String?= null;

    @Value("\${bot.url_api}")
    private val botUrlApi: String?= null;

    override fun sendMessage(body: Map<String?, Any?>?) {
        try {
            val `object` = body!!["object"] as Map<*, *>?
            val message = `object`!!["message"] as Map<*, *>?

            val text = message?.get("text") as String?
            val userId = message?.get("from_id") as Int?

            if (userId != null && text != null) {
                val randomId: Int = (System.currentTimeMillis() and 0xfffffffL).toInt()
                val url = String.format("%s?user_id=%s&message=%s&random_id=%d&access_token=%s&v=%s",
                    botUrlApi,
                    URLEncoder.encode(userId.toString(), StandardCharsets.UTF_8),
                    "Вы сказали: $text",
                    randomId,
                    URLEncoder.encode(botToken, StandardCharsets.UTF_8),
                    URLEncoder.encode(botVApi, StandardCharsets.UTF_8)
                );

                val response = restTemplate.postForObject(url, null, String::class.java)
                logger.info("Response from Bot API: {}", response)
            }
        } catch (e: Exception) {
            logger.error("Не удалось отправить сообщение", e)
        }
    }

}