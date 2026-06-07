package ru.vibeart.email.services;

import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * Сервис для отправки почтовых уведомлений (HTML-писем) пользователям.
 * <p>
 * Использует {@link JavaMailSender} для формирования и отправки {@link MimeMessage}.
 * Адрес отправителя берётся из конфигурации приложения (spring.mail.username).
 * </p>
 *
 * <h2>Возможности</h2>
 * <ul>
 *   <li>Отправка письма с кодом подтверждения регистрации;</li>
 *   <li>Отправка письма с временным паролем.</li>
 * </ul>
 *
 * <h2>Конфигурация</h2>
 * Требуются корректные SMTP-настройки Spring Boot:
 * <pre>
 * spring.mail.host=smtp.example.com
 * spring.mail.port=587
 * spring.mail.username=support@example.com
 * spring.mail.password=${MAIL_PASSWORD}
 * spring.mail.properties.mail.smtp.auth=true
 * spring.mail.properties.mail.smtp.starttls.enable=true
 * </pre>
 *
 * <h2>Особенности</h2>
 * <ul>
 *   <li>Методы не бросают исключений наружу — любые ошибки логируются на уровне WARN;</li>
 *   <li>Контент формируется в HTML с кодировкой UTF-8;</li>
 *   <li>Рекомендуется вынести HTML-шаблоны во внешние файлы/шаблонизатор (Thymeleaf/Freemarker).</li>
 * </ul>
 *
 * <h2>Безопасность</h2>
 * Отправка «временного пароля» по почте снижает безопасность.
 * Предпочтительно отправлять одноразовую ссылку на сброс пароля или OTP-код с ограниченным TTL.
 */
@Service
public class EmailService {

    /**
     * Почтовый адрес отправителя (обычно support/notify). Подставляется в поле From.
     */
    private final String supportEmail;

    /**
     * Почтовый клиент Spring для отправки MIME-сообщений.
     */
    private final JavaMailSender mailSender;

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    /**
     * Создаёт почтовый сервис.
     *
     * @param mailSender экземпляр {@link JavaMailSender}, сконфигурированный под SMTP
     * @param supportEmail адрес отправителя, как правило {@code ${spring.mail.username}} задается в {@code application.yml}.
     */
    public EmailService(JavaMailSender mailSender,
                        @Value("${spring.mail.username}") String supportEmail) {
        this.mailSender = mailSender;
        this.supportEmail = supportEmail;
    }

    /**
     * Отправляет письмо с кодом подтверждения регистрации.
     * <p>
     * Формирует HTML-письмо: заголовок, приветствие, выделенный код подтверждения,
     * пояснение об игнорировании письма, если запрос не пользователем.
     * </p>
     *
     * @param toEmail          адрес получателя
     * @param verificationCode код подтверждения (например, 6-значный OTP)
     *
     * <p><b>Ошибки:</b> Исключения не пробрасываются; при ошибке отправки будет записан
     * лог уровня WARN, письмо отправлено не будет.</p>
     */
    public void sendCustomVerificationEmail(String toEmail, String verificationCode) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(supportEmail);
            helper.setTo(toEmail);
            helper.setSubject("Подтверждение регистрации");

            String content =
                    "<html>" +
                            "<body style='font-family: Arial, sans-serif; background-color: #f9f9f9; padding: 20px;'>" +
                            "<div style='background-color: #fff; padding: 30px; border-radius: 8px; max-width: 600px; margin: auto;'>" +
                            "<h2 style='color: #4CAF50;'>Подтверждение регистрации</h2>" +
                            "<p>Здравствуйте! Спасибо за регистрацию в нашем сервисе.</p>" +
                            "<p>Ваш код подтверждения:</p>" +
                            "<div style='font-size: 24px; font-weight: bold; color: #4CAF50; margin: 20px 0;'>" + verificationCode + "</div>" +
                            "<p>Если вы не запрашивали это письмо, просто проигнорируйте его.</p>" +
                            "</div>" +
                            "</body>" +
                            "</html>";

            helper.setText(content, true);
            mailSender.send(mimeMessage);
        } catch (Exception e) {
            log.warn("Failed to send verification email to {}", toEmail, e);
        }
    }

    /**
     * Отправляет письмо с временным паролем.
     * <p>
     * Письмо содержит приветствие и выделенный временный пароль.
     * По умолчанию тема установлена как «Подтверждение регистрации».
     * Рекомендуется уточнить тему, например «Временный пароль для входа», чтобы она
     * соответствовала содержимому.
     * </p>
     *
     * @param toEmail  адрес получателя
     * @param password временный пароль (или одноразовый токен)
     *
     * <p><b>Безопасность:</b> по возможности используйте ссылку на сброс пароля вместо
     * отправки пароля в открытом виде.</p>
     *
     * <p><b>Ошибки:</b> Исключения не пробрасываются; при ошибке отправки будет записан
     * лог уровня WARN, письмо отправлено не будет.</p>
     */
    public void sendCustomPasswordEmail(String toEmail, String password) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(supportEmail);
            helper.setTo(toEmail);
            helper.setSubject("Подтверждение регистрации");

            String content = "<html>"
                    + "<body>"
                    + "<h1>Добро пожаловать!</h1>"
                    + "<p>Ваш временный пароль: <strong>" + password + "</strong></p>"
                    + "<p>Спасибо за регистрацию в нашем сервисе. Приятной работы!</p>"
                    + "</body>"
                    + "</html>";

            helper.setText(content, true);
            mailSender.send(mimeMessage);
        } catch (Exception e) {
            log.warn("Failed to send password email to {}", toEmail, e);
        }
    }
}
