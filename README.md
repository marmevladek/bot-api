# Bot API


## Функциональные возможности
- Обработка входящих сообщений через вебхук.
- Автоматические исходящие сообщения (попугай).
- Документирования API с использованием Swagger UI.
- Интеграция с **ngrok** для локального тестирование вебхука.

## Технологии
- **Java 17**
- **Spring Boot**
- **Springdoc OpenAPI**
- **ngrok**
- **Kotlin**

P.S. Ради интереса попробовал написать еще на Kotlin, раньше его не трогал.

---

## Установка и запуск

### 1. Клонирование проекта
Клонируйте репозиторий:
```bash
git clone https://github.com/marmevladek/bot-api.git
cd bot-api
```

### 2. Настройка окружения
Создайте локальный **application.yml** в папке /src/main/resources/ и вставьте в него содержимое из файла application-template.yml.
- **BOT_TOKEN** - ключ доступа для работы с API.
- **CONFIRM_CODE** - код, используемый для подтверждения адреса сервера, чтобы получать уведомления о событиях (сообщениях).
- **V_API** - версия API
- **URL_API** - url, используемый для исходящего сообщения.
```yaml
server:
  port: 8080

spring:
  application:
    name: BotApi

springdoc:
  api-docs:
    path: /v3/api-docs
  swagger-ui:
    path: /swagger-ui

logging:
  level:
    root: INFO
    org.springframework: WARN
    com.project.botapi: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"

bot:
  token: ${BOT_TOKEN}
  confirm_code: ${CONFIRM_CODE}
  v_api: ${V_API}
  url_api: ${URL_API}
 ```
### 3. Запуск приложения
Чтобы запустить приложение в локальной среде разработки, используйте команду:
```bash
./mvnw spring-boot:run
```
Cервер будет доступен по адресу http://localhost:8080.

### 4. Документация API (Swagger UI)
После запуска проекта, документация будет доступна по адресу: http://localhost:8080/swagger-ui/index.html#/

### 5. Тесты
Для запуска тестов используйте команду:
```bash
./mvnw test
```

---

## Использование ngrok
**ngrok** нужен для того, чтобы бот мог получать вебхуки на локальном сервере.

Шаги для настройки ngrok:
1. Скачайте и установите ngrok с официального сайта: ngrok.com.
2. Запустите ngrok, настроив его для перенаправления HTTP-трафика на порт 8080 (в нашем случае):
```bash
ngrok http 8080
```
3. После запуска вам будет предоставлен публичный URL вида https://random-symbols.ngrok-free.app ("random-symbols" - можно сказать уникальный идентификатор). Этот URL иожно использовать для настройки вебхука вашего бота в админ-панели группы ВК.

---

## Настройка бота и вебхука
1. Перейдите в управление группой во Вконтакте.
2. Получите код доступа: Настройки -> Работа с API -> Ключи доступа.
3. Настройте сервер: Настройки -> Работа с API -> Настройки сервера.

