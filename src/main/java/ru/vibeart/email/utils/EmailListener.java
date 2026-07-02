package ru.vibeart.email.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import ru.vibeart.email.services.EmailService;

import java.util.Map;

/**
 * Компонент, обрабатывающий входящие сообщения из очередей RabbitMQ.
 * <p>
 * Слушатель принимает сообщения в формате JSON, извлекает адрес электронной почты
 * и дополнительные параметры, после чего вызывает методы
 * {@link EmailService} для отправки соответствующих писем.
 * </p>
 *
 * <h2>Очереди:</h2>
 * <ul>
 *   <li><b>queueVerificationCodeEmail</b> — для писем с кодом подтверждения регистрации;</li>
 *   <li><b>queueVerificationCodeChangeEmail</b> — для писем с кодом подтверждения смены адреса электронной почты;</li>
 *  <li><b>queueVerificationCodeChangePassword</b> — для писем с кодом подтверждения пароля;</li>
 *   <li><b>queuePasswordEmail</b> — для писем с временным паролем.</li>
 * </ul>
 *
 * <h2>Формат ожидаемого JSON-сообщения</h2>
 * Для <b>queueVerificationCodeEmail</b>:
 * <pre>
 * {
 *   "email": "user@example.com",
 *   "verificationCode": "123456"
 * }
 * </pre>
 *
 * Для <b>queueVerificationCodeChangeEmail</b>:
 * <pre>
 * {
 *   "email": "user@example.com",
 *   "verificationCode": "123456"
 * }
 * </pre>
 *
 * Для <b>queueVerificationCodeChangePassword</b>:
 * <pre>
 * {
 *   "email": "user@example.com",
 *   "verificationCode": "123456"
 * }
 * </pre>
 *
 * Для <b>queuePasswordEmail</b>:
 * <pre>
 * {
 *   "email": "user@example.com",
 *   "password": "tempPass123"
 * }
 * </pre>
 *
 * <h2>Обработка ошибок</h2>
 * <ul>
 *   <li>Ошибки парсинга или отправки письма перехватываются и выводятся в консоль через {@code SLF4J}.</li>
 * </ul>
 *
 * <h2>Зависимости</h2>
 * <ul>
 *   <li>{@link EmailService} — для отправки писем;</li>
 *   <li>{@link ObjectMapper} — для десериализации JSON в {@link Map}.</li>
 * </ul>
 *
 * <h2>Пример интеграции</h2>
 * Отправка сообщения из другого сервиса:
 * <pre>
 * {
 *   "email": "test@example.com",
 *   "verificationCode": "482917"
 * }
 * </pre>
 * в очередь <b>queueVerificationCodeEmail</b> приведёт к вызову
 * {@code emailService.sendRegisterVerification("test@example.com", "482917")}.
 */
// Создание бина из этого класса
@Component
public class EmailListener {

    private final EmailService emailService;

    private final ObjectMapper objectMapper;

    private static final Logger log = LoggerFactory.getLogger(EmailListener.class);

    /**
     * Конструктор слушателя.
     *
     * @param emailService сервис отправки писем
     * @param objectMapper Jackson-десериализатор для чтения JSON
     */
    public EmailListener(EmailService emailService, ObjectMapper objectMapper) {
        this.emailService = emailService;
        this.objectMapper = objectMapper;
    }

    /**
     * Обрабатывает сообщения из очереди {@code queueVerificationCodeRegister}.
     * <p>
     * Извлекает поля {@code email} и {@code verificationCode} из JSON и вызывает
     * {@link EmailService#sendRegisterVerification(String, String, String)}.
     * </p>
     *
     * @param message JSON-строка с параметрами письма
     */
    @RabbitListener(queues = "queueVerificationCodeRegister")
    public void receiveRegisterVerificationCodeMessage(String message) {
        try {
            log.info("Received message: {}", message);
            Map<String, String> msgMap = objectMapper.readValue(message, Map.class);
            String email = msgMap.get("email");
            String verificationCode = msgMap.get("verificationCode");
            String language = msgMap.get("language");
            emailService.sendRegisterVerification(email, verificationCode, language);
        } catch (Exception ex) {
            log.error("Registration sending email error", ex);
        }
    }

    /**
     * Обрабатывает сообщения из очереди {@code queueVerificationCodeChangeEmail}.
     * <p>
     * Извлекает поля {@code email} и {@code verificationCode} из JSON и вызывает
     * {@link EmailService#sendChangeEmailVerification(String, String, String)}.
     * </p>
     *
     * @param message JSON-строка с параметрами письма
     */
    @RabbitListener(queues = "queueVerificationCodeChangeEmail")
    public void receiveChangeEmailVerificationCodeMessage(String message) {
        try {
            log.info("Received message: {}", message);
            // Преобразование JSON-строки в Map, чтобы иметь доступ к каждой строке и получать значение по ключу
            Map<String, String> msgMap = objectMapper.readValue(message, Map.class);
            String email = msgMap.get("email");
            String verificationCode = msgMap.get("verificationCode");
            String language = msgMap.get("language");
            emailService.sendChangeEmailVerification(email, verificationCode, language);
        } catch (Exception ex) {
            log.error("Change email sending email error", ex);
        }
    }

    /**
     * Обрабатывает сообщения из очереди {@code queueVerificationCodeChangePassword}.
     * <p>
     * Извлекает поля {@code email} и {@code verificationCode} из JSON и вызывает
     * {@link EmailService#sendChangePasswordVerification(String, String, String)}.
     * </p>
     *
     * @param message JSON-строка с параметрами письма
     */
    @RabbitListener(queues = "queueVerificationCodeChangePassword")
    public void receiveChangePasswordVerificationCodeMessage(String message) {
        try {
            log.info("Received message: {}", message);
            // Преобразование JSON-строки в Map, чтобы иметь доступ к каждой строке и получать значение по ключу
            Map<String, String> msgMap = objectMapper.readValue(message, Map.class);
            String email = msgMap.get("email");
            String verificationCode = msgMap.get("verificationCode");
            String language = msgMap.get("language");
            emailService.sendChangePasswordVerification(email, verificationCode, language);
        } catch (Exception ex) {
            log.error("Change password sending email error", ex);
        }
    }

    /**
     * Обрабатывает сообщения из очереди {@code queuePasswordEmail}.
     * <p>
     * Извлекает поля {@code email} и {@code password} из JSON и вызывает
     * {@link EmailService#sendCustomPasswordEmail(String, String)}.
     * </p>
     *
     * @param message JSON-строка с параметрами письма
     */
    @RabbitListener(queues = "queuePasswordEmail")
    public void receivePasswordMessage(String message) {
        try {
            log.info("Received message: {}", message);
            Map<String, String> msgMap = objectMapper.readValue(message, Map.class);
            String email = msgMap.get("email");
            String password = msgMap.get("password");
            emailService.sendCustomPasswordEmail(email, password);
        } catch (Exception ex) {
            log.error("Change password sending email error", ex);
        }
    }
}
