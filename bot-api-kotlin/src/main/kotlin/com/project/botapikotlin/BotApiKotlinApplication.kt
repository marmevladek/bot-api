package com.project.botapikotlin

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.retry.annotation.EnableRetry

@SpringBootApplication
@EnableRetry
class BotApiKotlinApplication

fun main(args: Array<String>) {
    runApplication<BotApiKotlinApplication>(*args)
}
