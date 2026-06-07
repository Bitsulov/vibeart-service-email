# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Команды

```bash
# Сборка
./gradlew build

# Запуск
./gradlew bootRun

# Запуск тестов
./gradlew test

# Запуск одного тестового класса
./gradlew test --tests "ru.vibeart.email.EmailServiceApplicationTests"

# Сборка без тестов
./gradlew build -x test
```

## Переменные окружения

Сервис требует наличия файла `.env` (загружается через `optional:file:.env[.properties]`) со следующими переменными:

```
SUPPORT_EMAIL=...
EMAIL_PASSWORD=...
RABBIT_MQ_HOST=...
RABBIT_MQ_USERNAME=...
RABBIT_MQ_PASSWORD=...
RABBIT_MQ_VHOST=...
```

## Архитектура

Это микросервис на **Spring Boot 4.0.0 / Java 25** в составе платформы VibeArt. Его единственная задача — отправка транзакционных HTML-писем, инициируемая сообщениями из RabbitMQ.

**Поток сообщений:**
1. Другой сервис публикует JSON-сообщение в `exchange` (TopicExchange) с указанием routing key.
2. `EmailListener` читает сообщение из привязанной очереди и десериализует JSON в `Map<String, String>`.
3. `EmailService` формирует HTML-письмо и отправляет его через JavaMailSender по SMTP (smtp.mail.ru:587, STARTTLS).

**Топология RabbitMQ** (объявлена в `RabbitMqConfig`):
- Обменник: `exchange` (TopicExchange, non-durable)
- Очередь `queueVerificationCodeEmail` ← routing key `emailVerificationCode.key`
  - Поля сообщения: `email`, `verificationCode`
- Очередь `queuePasswordEmail` ← routing key `emailPassword.key`
  - Поля сообщения: `email`, `password`

**Важные детали:**
- Очереди и exchange не сохраняются при перезапуске брокера (`durable=false`).
- `EmailService` перехватывает все исключения внутри и логирует их на уровне WARN — наружу они не пробрасываются.
- HTML-шаблоны писем собираются как inline-строки прямо в `EmailService` — шаблонизатор не используется.
- Actuator открывает эндпоинты `/actuator/health`, `/actuator/info`, `/actuator/prometheus` на порту 8081.
- Запрещено читать .env
- Github Flow. Ветки именуются по функциональности: `feature/<название>`
- Заголовок: не более 50 символов, на английском, начинается с глагола (Add, Fix, Update, Remove)
- Описание: не более 72 символов в строке, на английском, объясняет что и зачем сделано
- Каждый коммит — одна логическая единица изменений
