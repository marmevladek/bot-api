package com.project.botapi.service;

import java.util.Map;

public interface BotApiService {

    void sendMessage(Map<String, Object> body);
}
