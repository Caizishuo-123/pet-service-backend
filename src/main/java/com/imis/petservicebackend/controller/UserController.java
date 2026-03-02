package com.imis.petservicebackend.controller;

import com.imis.petservicebackend.common.BusinessException;
import com.imis.petservicebackend.common.Result;
import com.imis.petservicebackend.entity.UserInfo;
import com.imis.petservicebackend.service.CosService;
import com.imis.petservicebackend.service.EmailCodeRedisService;
import com.imis.petservicebackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.imis.petservicebackend.entity.User;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private CosService cosService;

    @Autowired
    private EmailCodeRedisService emailCodeRedisService;

    @PostMapping("/login")
    public Result<?> login(@RequestParam("account") String account, @RequestParam("password") String password) {
        return Result.success(userService.login(account, password));
    }

    @PostMapping("/register")
    public Result<?> register(@RequestBody User user) {
        boolean result = userService.register(user);
        return Result.success(result);
    }

    @GetMapping("/current-user-info")
    public Result<UserInfo> getCurrentUserInfo(@RequestAttribute("account") String account) {
        return Result.success(userService.getCurrentUserInfo(account));
    }

    @PostMapping("/logout")
    public Result<String> logout() {
        // JWT 无状态，服务器无需处理，前端删除 token 即可
        return Result.success("退出成功");
    }

    /**
     * 修改个人信息（地址）
     * 手机号不可修改
     */
    @PutMapping("/update-info")
    public Result<?> updateInfo(@RequestAttribute("account") String account,
            @RequestParam("address") String address) {
        boolean result = userService.updateUserInfo(account, address);
        return Result.success(result);
    }

    /**
     * 上传/更新头像（上传到腾讯云 COS）
     */
    @PostMapping("/update-avatar")
    public Result<?> updateAvatar(@RequestAttribute("account") String account,
            @RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }
        try {
            // 上传到 COS 的 upload/head 目录
            String avatarUrl = cosService.upload(file, "upload/head");
            // 更新数据库头像字段
            userService.updateAvatar(account, avatarUrl);
            return Result.success(avatarUrl);
        } catch (Exception e) {
            log.error("头像上传失败", e);
            throw new BusinessException("头像上传失败，请稍后重试");
        }
    }

    /**
     * 修改密码（登录状态，通过邮箱验证码验证）
     * 前端需先调用 /email/send-code 获取验证码
     */
    @PutMapping("/update-password")
    public Result<?> updatePassword(@RequestAttribute("account") String account,
            @RequestParam("email") String email,
            @RequestParam("code") String code,
            @RequestParam("newPassword") String newPassword,
            @RequestParam("confirmPassword") String confirmPassword) {
        // 校验邮箱验证码
        if (!emailCodeRedisService.verifyCode(email, code)) {
            throw new BusinessException("验证码错误或已过期");
        }
        boolean result = userService.updatePasswordByEmail(account, email, newPassword, confirmPassword);
        // 修改成功后删除验证码
        emailCodeRedisService.deleteCode(email);
        return Result.success(result);
    }
}
