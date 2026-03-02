package com.imis.petservicebackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.imis.petservicebackend.common.BusinessException;
import com.imis.petservicebackend.entity.User;
import com.imis.petservicebackend.entity.UserInfo;
import com.imis.petservicebackend.service.UserService;
import com.imis.petservicebackend.mapper.UserMapper;
import com.imis.petservicebackend.utlis.CommonUtil;
import com.imis.petservicebackend.utlis.JwtUtil;
import com.imis.petservicebackend.utlis.MD5Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author 64360
 * @description 针对表【user(用户表)】的数据库操作Service实现
 * @createDate 2026-02-02 15:22:54
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public String login(String account, String password) {
        LambdaQueryWrapper<User> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(User::getUsername, account)
                .or()
                .eq(User::getPhone, account)
                .or()
                .eq(User::getEmail, account);
        User user = userMapper.selectOne(lambdaQueryWrapper);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        // 密码校验
        if (!MD5Util.matches(password, user.getPassword())) {
            throw new BusinessException("账号或密码错误");
        }
        return JwtUtil.generateToken(user.getUsername());
    }

    @Override
    public boolean register(User user) {
        LambdaQueryWrapper<User> lambdaQueryWrapper1 = new LambdaQueryWrapper<>();
        lambdaQueryWrapper1.eq(User::getUsername, user.getUsername());
        if (userMapper.selectCount(lambdaQueryWrapper1) > 0) {
            throw new BusinessException("用户名已存在");
        }
        LambdaQueryWrapper<User> lambdaQueryWrapper2 = new LambdaQueryWrapper<>();
        lambdaQueryWrapper2.eq(User::getEmail, user.getEmail());
        if (userMapper.selectCount(lambdaQueryWrapper2) > 0) {
            throw new BusinessException("邮箱已存在");
        }
        user.setPassword(MD5Util.encrypt(user.getPassword()));
        // 随机分配头像
        user.setAvatar(CommonUtil.getRandomAvatar());
        return userMapper.insert(user) > 0;
    }

    @Override
    public UserInfo getCurrentUserInfo(String account) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, account);
        User user = userMapper.selectOne(wrapper);
        UserInfo userInfo = new UserInfo();
        if (user != null) {
            userInfo.setUsername(user.getUsername());
            userInfo.setPhone(CommonUtil.maskPhone(user.getPhone()));
            userInfo.setEmail(user.getEmail());
            userInfo.setAvatar(user.getAvatar());
            userInfo.setAddress(user.getAddress());
        }
        return userInfo;
    }

    @Override
    public boolean resetPassword(String email, String newPassword, String confirmPassword) {
        if (!newPassword.equals(confirmPassword)) {
            throw new BusinessException("两次密码不一致");
        }
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getEmail, email);
        User user = userMapper.selectOne(wrapper);
        if (user == null) {
            throw new BusinessException("该邮箱未注册");
        }
        user.setPassword(MD5Util.encrypt(newPassword));
        return userMapper.updateById(user) > 0;
    }

    @Override
    public boolean updatePasswordByEmail(String username, String email, String newPassword, String confirmPassword) {
        if (!newPassword.equals(confirmPassword)) {
            throw new BusinessException("两次密码不一致");
        }
        // 验证邮箱是否属于当前用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        User user = userMapper.selectOne(wrapper);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (!email.equals(user.getEmail())) {
            throw new BusinessException("邮箱与当前账号不匹配");
        }
        user.setPassword(MD5Util.encrypt(newPassword));
        return userMapper.updateById(user) > 0;
    }

    @Override
    public boolean updateUserInfo(String username, String address) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        User user = userMapper.selectOne(wrapper);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (address != null) {
            user.setAddress(address);
        }
        return userMapper.updateById(user) > 0;
    }

    @Override
    public boolean updateAvatar(String username, String avatarUrl) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        User user = userMapper.selectOne(wrapper);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setAvatar(avatarUrl);
        return userMapper.updateById(user) > 0;
    }

}
