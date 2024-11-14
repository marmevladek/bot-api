package com.project.botapikotlin.service

interface BotApiService {
    fun sendMessage(body: Map<String?, Any?>) : String
}