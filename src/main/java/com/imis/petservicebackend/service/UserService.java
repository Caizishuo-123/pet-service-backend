package com.imis.petservicebackend.service;

import com.imis.petservicebackend.entity.User;
import com.imis.petservicebackend.entity.UserInfo;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author 64360
 * @description 针对表【user(用户表)】的数据库操作Service
 * @createDate 2026-02-02 15:22:54
 */
public interface UserService extends IService<User> {

    java.util.Map<String, Object> login(String account, String password);

    boolean register(User user);

    UserInfo getCurrentUserInfo(String account);

    /**
     * 忘记密码 —— 通过邮箱重置
     */
    boolean resetPassword(String email, String newPassword, String confirmPassword);

    /**
     * 修改密码 —— 登录状态下，通过邮箱验证码
     */
    boolean updatePasswordByEmail(String username, String email, String newPassword, String confirmPassword);

    /**
     * 修改个人信息（地址）
     */
    boolean updateUserInfo(String username, String address);

    /**
     * 更新头像
     */
    boolean updateAvatar(String username, String avatarUrl);
}
