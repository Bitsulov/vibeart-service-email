package ru.vibeart.email.configs;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурация общих бинов приложения.
 * <p>
 * Содержит настройку:
 * <ul>
 *     <li>{@link ObjectMapper} — для десериализации JSON-сообщений из RabbitMQ в {@link java.util.Map}.</li>
 * </ul>
 *
 * <h2>Назначение</h2>
 * Данный класс используется как общий Spring-конфиг, доступный во всех компонентах сервиса.
 * </p>
 */
@Configuration
public class BeanConfig {
    /**
     * Бин {@link ObjectMapper} для десериализации JSON-сообщений из RabbitMQ.
     * <p>
     * Пример использования:
     * <pre>
     * Map&lt;String, String&gt; map = objectMapper.readValue(json, Map.class);
     * </pre>
     *
     * @return экземпляр {@link ObjectMapper}
     */
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
