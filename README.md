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

| Переменная           | Описание                                            |
|----------------------|-----------------------------------------------------|
| `SUPPORT_EMAIL`      | Адрес отправителя (поле From)                       |
| `EMAIL_PASSWORD`     | Пароль от SMTP-аккаунта                             |
| `EMAIL_HOST`         | SMTP-сервер для отправки писем                      |
| `EMAIL_PORT`         | Порт SMTP-сервера                                   |
| `EMAIL_TLS`          | Включение TLS-шифрования для писем (true или false) |
| `RABBIT_MQ_HOST`     | Хост брокера RabbitMQ                               |
| `RABBIT_MQ_USERNAME` | Логин RabbitMQ                                      |
| `RABBIT_MQ_PASSWORD` | Пароль RabbitMQ                                     |
| `RABBIT_MQ_VHOST`    | Виртуальный хост RabbitMQ                           |

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

**Шаблонизатор и генерация HTML-писем**
- [Thymeleaf](https://www.thymeleaf.org/) — шаблонизатор для HTML-писем

---

## Архитектура

Сервис не предоставляет HTTP API. Все входящие команды поступают через RabbitMQ.

### Поток сообщений:
1. Другой сервис публикует JSON-сообщение в exchange с routing key.
2. RabbitMQ маршрутизирует сообщение в соответствующую очередь.
3. `EmailListener` читает сообщение и десериализует JSON.
4. `EmailService` формирует HTML-письмо и отправляет его через SMTP (smtp.mail.ru:587).

### Топология RabbitMQ:

| Очередь                               | Routing key                          | Поля сообщения                          |
|---------------------------------------|--------------------------------------|-----------------------------------------|
| `queueVerificationCodeRegister`       | `RegisterVerificationCode.key`       | `email`, `verificationCode`, `language` |
| `queueVerificationCodeChangeEmail`    | `ChangeEmailVerificationCode.key`    | `email`, `verificationCode`, `language` |
| `queueVerificationCodeChangePassword` | `ChangePasswordVerificationCode.key` | `email`, `verificationCode`, `language` |
| `queuePasswordEmail`                  | `emailPassword.key`                  | `email`, `password`                     |

---

## Docker

Проект использует многоэтапный Dockerfile:

- **Build** — сборка jar-файла через Gradle
- **Run** — запуск на минимальном JRE-образе

---

## Ссылки

- [VibeArt](https://github.com/Bitsulov/VibeArt.git) — основной репозиторий
- [VibeArt Web Frontend](https://github.com/Bitsulov/vibeart-web-frontend.git) — Клиентское web-приложение
- [VibeArt API](https://github.com/Bitsulov/vibeart-service-api.git) — основной сервис API
