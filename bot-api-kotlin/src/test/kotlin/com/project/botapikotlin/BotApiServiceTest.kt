package com.project.botapikotlin

import com.project.botapikotlin.service.BotApiService
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.Executable
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import java.net.URI
import java.util.Map

@SpringBootTest
class BotApiServiceTest {

    @MockBean
    private val restTemplate: RestTemplate? = null

    @Autowired
    private val botApiService: BotApiService? = null

    @Test
    fun testSendMessageInvalidAccessToken() {

        Mockito.`when`(
            restTemplate!!.postForObject(
                Mockito.any(URI::class.java), Mockito.isNull(), Mockito.eq(
                    String::class.java
                )
            )
        )
            .thenReturn("ok")

        val body = Map.of<String?, Any?>(
            "object", Map.of("message", Map.of("text", "Hello", "from_id", 123, "access_token", "2345y6"))
        )

        Assertions.assertThrows(RestClientException::class.java) {
            botApiService!!.sendMessage(
                body
            )
        }
    }

    @Test
    fun testHandleInvalidBody() {
        val invalidBody = Map.of<String, Any?>()

        assertThrows(IllegalArgumentException::class.java) { botApiService!!.sendMessage(invalidBody) }
    }
}