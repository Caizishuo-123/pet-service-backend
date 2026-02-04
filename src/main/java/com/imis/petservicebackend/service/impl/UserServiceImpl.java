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
                .eq(User::getPhone, account);
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
        lambdaQueryWrapper2.eq(User::getPhone, user.getPhone());
        if (userMapper.selectCount(lambdaQueryWrapper2) > 0) {
            throw new BusinessException("手机号已存在");
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
            userInfo.setAvatar(user.getAvatar());
            userInfo.setAddress(user.getAddress());
        }
        return userInfo;
    }

    @Override
    public boolean verifyPhone(String userName, String phone) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, userName)
                .eq(User::getPhone, phone);
        return userMapper.selectCount(wrapper) > 0;
    }

    @Override
    public boolean updatePassword(String userName, String password, String repeatPassword) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, userName);
        User user = userMapper.selectOne(wrapper);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (!password.equals(repeatPassword)) {
            throw new BusinessException("两次密码不一致");
        }
        user.setPassword(MD5Util.encrypt(password));
        return userMapper.updateById(user) > 0;
    }



}
