# VibeArt — Email Service

Микросервис отправки транзакционных писем: коды подтверждения и временные пароли.

> Этот репозиторий является подмодулем основного репозитория [VibeArt](https://github.com/Bitsulov/VibeArt.git), который запускается через Docker Compose.

---

## Требования

- **JDK 25+** — среда разработки Java, необходима для сборки и запуска сервиса
- **RabbitMQ** — брокер сообщений, необходим для получения команд на отправку писем
- **SMTP-аккаунт** — почтовый ящик для отправки писем

---

## Быстрый старт

Сервис рассчитан на запуск через Docker Compose из основного репозитория.  
Для локального запуска скопируй `.env.example` в `.env` и заполни переменные:

```bash
cp .env.example .env
```

| Переменная           | Описание                      |
|----------------------|-------------------------------|
| `SUPPORT_EMAIL`      | Адрес отправителя (поле From) |
| `EMAIL_PASSWORD`     | Пароль от SMTP-аккаунта       |
| `RABBIT_MQ_HOST`     | Хост брокера RabbitMQ         |
| `RABBIT_MQ_USERNAME` | Логин RabbitMQ                |
| `RABBIT_MQ_PASSWORD` | Пароль RabbitMQ               |
| `RABBIT_MQ_VHOST`    | Виртуальный хост RabbitMQ     |

```bash
./gradlew bootRun
```

---

## Команды

| Команда                   | Описание           |
|---------------------------|--------------------|
| `./gradlew build`         | Сборка проекта     |
| `./gradlew build -x test` | Сборка без тестов  |
| `./gradlew bootRun`       | Запуск сервиса     |
| `./gradlew test`          | Запуск всех тестов |

---

## Стек технологий

**Основное**
- [Java 25](https://openjdk.org/) + [Spring Boot 4.0](https://spring.io/projects/spring-boot)
- [Spring AMQP](https://spring.io/projects/spring-amqp) — получение сообщений из RabbitMQ
- [Spring Mail](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/mail/javamail/JavaMailSender.html) — отправка писем по SMTP

**Мониторинг**
- [Spring Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html) — проверка состояния сервиса и метрики
- [Micrometer + Prometheus](https://micrometer.io/) — сбор и экспорт метрик

**Тестирование**
- [JUnit 5](https://junit.org/junit5/) — модульные и интеграционные тесты

---

## Архитектура

Сервис не предоставляет HTTP API. Все входящие команды поступают через RabbitMQ.

### Поток сообщений:
1. Другой сервис публикует JSON-сообщение в exchange с routing key.
2. RabbitMQ маршрутизирует сообщение в соответствующую очередь.
3. `EmailListener` читает сообщение и десериализует JSON.
4. `EmailService` формирует HTML-письмо и отправляет его через SMTP (smtp.mail.ru:587).

### Топология RabbitMQ:

| Очередь                      | Routing key                 | Поля сообщения              |
|------------------------------|-----------------------------|-----------------------------|
| `queueVerificationCodeEmail` | `emailVerificationCode.key` | `email`, `verificationCode` |
| `queuePasswordEmail`         | `emailPassword.key`         | `email`, `password`         |

---

## Docker

Проект использует многоэтапный Dockerfile:

- **Build** — сборка jar-файла через Gradle
- **Run** — запуск на минимальном JRE-образе

---

## Ссылки

- [VibeArt](https://github.com/Bitsulov/VibeArt.git) — основной репозиторий
- [VibeArt-frontend](https://github.com/Bitsulov/VibeArt-frontend) — Клиентское web-приложение
- [VibeArt Backend](https://github.com/Bitsulov/vibeart-backend.git) — основной сервис API
