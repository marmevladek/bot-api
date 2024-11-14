package com.project.botapikotlin.service.impl

import com.project.botapikotlin.service.BotApiService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Recover
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import java.net.ConnectException
import java.net.URI
import java.util.*

@Service
class BotApiServiceImpl(
    restTemplateBuilder: RestTemplateBuilder,
) : BotApiService {

    companion object  {
        private val logger = LoggerFactory.getLogger(BotApiServiceImpl::class.java)
    }

    private val restTemplate: RestTemplate = restTemplateBuilder.build();


    @Value("\${bot.token}")
    private val botToken: String? = null

    @Value("\${bot.v_api}")
    private val botVApi: String? = null

    @Value("\${bot.url_api}")
    private val botUrlApi: String? = null


    @Retryable(
        retryFor = [RestClientException::class, HttpClientErrorException::class, ConnectException::class],
        backoff = Backoff(delay = 2000, multiplier = 2.0)
    )
    override fun sendMessage(body: Map<String?, Any?>): String {
        val `object`: Map<String, Any>?
        val message: Map<String, Any>?
        val text: String?
        val userId: Int?

        try {
            `object` = body["object"] as Map<String, Any>?
            requireNotNull(`object`) { "Некорректный формат входных данных: 'object' is null" }

            message = `object`["message"] as Map<String, Any>?
            requireNotNull(message) { "Некорректный формат входных данных: 'message' is null" }

            text = message["text"] as String?
            requireNotNull(text) { "Некорректный формат входных данных: 'text' is null" }

            userId = message["from_id"] as Int?
            requireNotNull(userId) { "Некорректный формат входных данных: 'from_id' is null" }
        } catch (e: ClassCastException) {
            logger.error("ClassCastExc | Некорректный формат входных данных: {}", e.message)
            throw IllegalArgumentException("Некорректный формат входных данных", e)
        } catch (e: NullPointerException) {
            logger.error("NullPointerException | Некорректный формат входных данных: {}", e.message)
            throw IllegalArgumentException("Некорректный формат входных данных", e)
        }

        val messageText = "Вы сказали: $text"
        val randomId = UUID.randomUUID().hashCode()

        val url = buildMessageUri(userId, messageText, randomId)

        logger.debug("Отправка запроса на URL: {}", url)

        try {
            val response = restTemplate.postForObject(url, null, String::class.java)

            if (response != null && response.contains("error")) {
                logger.error("Ошибка: {}", response)
                throw RestClientException("Ошибка: $response")
            }
        } catch (e: HttpClientErrorException) {
            logger.error("Ошибка HTTP: {}", e.message)
            throw e
        } catch (e: RestClientException) {
            logger.error("Ошибка запроса: {}", e.message)
            throw e
        } catch (e: Exception) {
            logger.error("Необработанная ошибка: {}", e.message)
            throw RuntimeException("Необработанная ошибка при отправке сообщения", e)
        }

        return "ok"
    }

    @Recover
    private fun recover(e: HttpClientErrorException, body: Map<String, Any>): String {
        logger.error(
            "HttpClientErrorException | Не удалось отправить сообщение после нескольких попыток: {}",
            e.message
        )
        return "Не удалось отправить сообщение. Пожалуйста, попробуйте позже."
    }

    @Recover
    private fun recover(e: RestClientException, body: Map<String, Any>): String {
        logger.error("RestClientException | Не удалось отправить сообщение после нескольких попыток: {}", e.message)
        return "Не удалось отправить сообщение. Пожаоуйста попробуйте позже."
    }


    private fun buildMessageUri(userId: Int, messageText: String, randomId: Int): URI {
        return UriComponentsBuilder.fromHttpUrl(botUrlApi!!)
            .queryParam("user_id", userId)
            .queryParam("message", messageText)
            .queryParam("random_id", randomId)
            .queryParam("access_token", botToken)
            .queryParam("v", botVApi)
            .build()
            .toUri()
    }

}