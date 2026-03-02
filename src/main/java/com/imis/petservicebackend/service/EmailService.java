package com.imis.petservicebackend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

  private final JavaMailSender javaMailSender;

  @Value("${spring.mail.username}")
  private String fromEmail;

  /**
   * 生成6位随机验证码
   */
  public String generateCode() {
    return String.format("%06d", new Random().nextInt(1000000));
  }

  /**
   * 发送验证码邮件
   *
   * @param toEmail 收件人邮箱
   * @param code    验证码
   */
  public void sendVerificationCode(String toEmail, String code) {
    SimpleMailMessage message = new SimpleMailMessage();
    message.setFrom(fromEmail);
    message.setTo(toEmail);
    message.setSubject("【宠物服务平台】邮箱验证码");
    message.setText("您好！\n\n您的验证码是：" + code
        + "\n\n该验证码 5 分钟内有效，请勿将验证码泄露给他人。\n\n如非本人操作，请忽略此邮件。");
    javaMailSender.send(message);
    log.info("验证码邮件已发送至：{}", toEmail);
  }
}
