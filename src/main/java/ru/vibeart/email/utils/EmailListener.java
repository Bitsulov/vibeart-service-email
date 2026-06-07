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
 * и дополнительные параметры (код подтверждения или пароль), после чего вызывает методы
 * {@link EmailService} для отправки соответствующих писем.
 * </p>
 *
 * <h2>Очереди:</h2>
 * <ul>
 *   <li><b>queueVerificationCodeEmail</b> — для писем с кодом подтверждения регистрации;</li>
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
 *   <li>Ошибки парсинга или отправки письма перехватываются и выводятся в консоль через {@code ex.printStackTrace()}.</li>
 *   <li>Рекомендуется заменить вывод стека на логирование через SLF4J.</li>
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
 * {@code emailService.sendCustomVerificationEmail("test@example.com", "482917")}.
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
     * @param emailService  сервис отправки писем
     * @param objectMapper  Jackson-десериализатор для чтения JSON
     */
    public EmailListener(EmailService emailService, ObjectMapper objectMapper) {
        this.emailService = emailService;
        this.objectMapper = objectMapper;
    }

    /**
     * Обрабатывает сообщения из очереди {@code queueVerificationCodeEmail}.
     * <p>
     * Извлекает поля {@code email} и {@code verificationCode} из JSON и вызывает
     * {@link EmailService#sendCustomVerificationEmail(String, String)}.
     * </p>
     *
     * @param message JSON-строка с параметрами письма
     */
    @RabbitListener(queues = "queueVerificationCodeEmail")
    public void receiveVerificationCodeMessage(String message) {
        try {
            log.info("Получено сообщение: {}", message);
            // Преобразование JSON-строки в Map, чтобы иметь доступ к каждой строке и получать значение по ключу
            Map<String, String> msgMap = objectMapper.readValue(message, Map.class);
            String email = msgMap.get("email");
            String verificationCode = msgMap.get("verificationCode");
            emailService.sendCustomVerificationEmail(email, verificationCode);
        } catch (Exception ex) {
            ex.printStackTrace();
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
            System.out.println("Получено сообщение: " + message);
            Map<String, String> msgMap = objectMapper.readValue(message, Map.class);
            String email = msgMap.get("email");
            String password = msgMap.get("password");
            emailService.sendCustomPasswordEmail(email, password);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
