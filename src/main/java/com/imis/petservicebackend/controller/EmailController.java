package com.imis.petservicebackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.imis.petservicebackend.common.BusinessException;
import com.imis.petservicebackend.common.Result;
import com.imis.petservicebackend.entity.User;
import com.imis.petservicebackend.service.EmailCodeRedisService;
import com.imis.petservicebackend.service.EmailService;
import com.imis.petservicebackend.service.UserService;
import com.imis.petservicebackend.utlis.CommonUtil;
import com.imis.petservicebackend.utlis.MD5Util;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/email")
@Slf4j
public class EmailController {

  @Autowired
  private EmailService emailService;

  @Autowired
  private EmailCodeRedisService emailCodeRedisService;

  @Autowired
  private UserService userService;

  /**
   * 发送邮箱验证码
   *
   * @param email 目标邮箱
   */
  @PostMapping("/send-code")
  public Result<?> sendCode(@RequestParam("email") String email) {
    // 简单校验邮箱格式
    if (email == null || !email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
      throw new BusinessException("邮箱格式不正确");
    }

    // 生成验证码
    String code = emailService.generateCode();

    // 存入 Redis（5分钟有效）
    emailCodeRedisService.saveCode(email, code);

    // 发送邮件
    try {
      emailService.sendVerificationCode(email, code);
    } catch (Exception e) {
      log.error("邮件发送失败", e);
      throw new BusinessException("验证码发送失败，请稍后重试");
    }

    return Result.success("验证码已发送，请查收邮箱");
  }

  /**
   * 邮箱验证码注册
   *
   * @param email    邮箱
   * @param code     验证码
   * @param username 用户名
   * @param phone    手机号
   * @param password 密码
   */
  @PostMapping("/register")
  public Result<?> registerByEmail(@RequestParam("email") String email,
      @RequestParam("code") String code,
      @RequestParam("username") String username,
      @RequestParam("phone") String phone,
      @RequestParam("password") String password) {
    // 1. 校验验证码
    if (!emailCodeRedisService.verifyCode(email, code)) {
      throw new BusinessException("验证码错误或已过期");
    }

    // 2. 校验手机号格式
    if (phone == null || !phone.matches("^1[3-9]\\d{9}$")) {
      throw new BusinessException("手机号格式不正确");
    }

    // 3. 检查邮箱是否已注册
    LambdaQueryWrapper<User> emailWrapper = new LambdaQueryWrapper<>();
    emailWrapper.eq(User::getEmail, email);
    if (userService.count(emailWrapper) > 0) {
      throw new BusinessException("该邮箱已被注册");
    }

    // 4. 检查用户名是否已注册
    LambdaQueryWrapper<User> usernameWrapper = new LambdaQueryWrapper<>();
    usernameWrapper.eq(User::getUsername, username);
    if (userService.count(usernameWrapper) > 0) {
      throw new BusinessException("用户名已存在");
    }

    // 5. 检查手机号是否已注册
    LambdaQueryWrapper<User> phoneWrapper = new LambdaQueryWrapper<>();
    phoneWrapper.eq(User::getPhone, phone);
    if (userService.count(phoneWrapper) > 0) {
      throw new BusinessException("该手机号已被注册");
    }

    // 6. 创建用户
    User user = new User();
    user.setEmail(email);
    user.setUsername(username);
    user.setPhone(phone);
    user.setPassword(MD5Util.encrypt(password));
    user.setAvatar(CommonUtil.getRandomAvatar());

    boolean saved = userService.save(user);
    if (!saved) {
      throw new BusinessException("注册失败，请稍后重试");
    }

    // 7. 注册成功后删除验证码
    emailCodeRedisService.deleteCode(email);

    return Result.success("注册成功");
  }

  /**
   * 忘记密码 —— 通过邮箱验证码重置密码（不需要登录）
   * 前端需先调用 /email/send-code 获取验证码
   *
   * @param email           邮箱
   * @param code            验证码
   * @param newPassword     新密码
   * @param confirmPassword 确认密码
   */
  @PostMapping("/reset-password")
  public Result<?> resetPassword(@RequestParam("email") String email,
      @RequestParam("code") String code,
      @RequestParam("newPassword") String newPassword,
      @RequestParam("confirmPassword") String confirmPassword) {
    // 1. 校验验证码
    if (!emailCodeRedisService.verifyCode(email, code)) {
      throw new BusinessException("验证码错误或已过期");
    }

    // 2. 重置密码
    userService.resetPassword(email, newPassword, confirmPassword);

    // 3. 重置成功后删除验证码
    emailCodeRedisService.deleteCode(email);

    return Result.success("密码重置成功");
  }
}
