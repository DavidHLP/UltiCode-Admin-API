package com.david.auth.support;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;

/**
 * 提供发送 HTML 邮件的公共能力： - 自动识别并使用默认发件人（JavaMailSenderImpl.getUsername） - 统一创建 MimeMessage 和
 * MimeMessageHelper - 暴露简洁的 sendHtml 接口
 */
@Component
public class EmailSupport {

    private final JavaMailSender mailSender;
    private final String defaultFromAddress;

    public EmailSupport(JavaMailSender mailSender) {
        this.mailSender = mailSender;
        if (mailSender instanceof JavaMailSenderImpl senderImpl) {
            this.defaultFromAddress = senderImpl.getUsername();
        } else {
            this.defaultFromAddress = null;
        }
    }

    /** 发送 HTML 邮件。抛出的 MessagingException / MailException 交由上层处理（用于回滚等）。 */
    public void sendHtml(@NonNull String to, @NonNull String subject, @NonNull String htmlBody)
            throws MessagingException, MailException {

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper =
                new MimeMessageHelper(
                        message,
                        MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                        StandardCharsets.UTF_8.name());

        if (StringUtils.hasText(defaultFromAddress)) {
            helper.setFrom(defaultFromAddress);
        }
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlBody, true);

        mailSender.send(message);
    }

    /** 小工具：以 String.format 渲染模板。 */
    public String render(@NonNull String template, @Nullable Object... args) {
        return String.format(template, args);
    }
}
