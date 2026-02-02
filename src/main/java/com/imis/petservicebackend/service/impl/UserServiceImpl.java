package com.imis.petservicebackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.imis.petservicebackend.common.BusinessException;
import com.imis.petservicebackend.entity.LoginVO;
import com.imis.petservicebackend.entity.User;
import com.imis.petservicebackend.service.UserService;
import com.imis.petservicebackend.mapper.UserMapper;
import com.imis.petservicebackend.utlis.JwtUtil;
import com.imis.petservicebackend.utlis.MD5Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Random;

/**
* @author 64360
* @description 针对表【user(用户表)】的数据库操作Service实现
* @createDate 2026-02-02 15:22:54
*/
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{

    @Autowired
    private UserMapper userMapper;

    @Override
    public LoginVO login(String account, String password) {
        LambdaQueryWrapper<User> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(User::getUsername , account)
                .or()
                .eq(User::getPhone , account);
        User user = userMapper.selectOne(lambdaQueryWrapper);
        if(user == null){
            throw new BusinessException("用户不存在");
        }
        // 密码校验
        if (!MD5Util.matches(password, user.getPassword())) {
            throw new BusinessException("账号或密码错误");
        }
        // 生成 token
        // 组装返回对象
        LoginVO vo = new LoginVO();
        vo.setToken(JwtUtil.generateToken(user.getUsername()));
        vo.setUserId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setPhone(maskPhone(user.getPhone()));
        vo.setRole(user.getRole());
        return vo;
    }


    /**
     * 随机获取头像路径
     */
    private String getRandomAvatar() {
        // 头像编号 1-3
        int index = new Random().nextInt(10); // 0 ~ 9
        // 返回数据库存储的路径，前端访问即可
        return "/img/" + index + ".jpg";
    }

    /**
     * 隐藏手机号
     * @param phone
     * @return String
     */
    private String maskPhone(String phone) {
        if (phone == null || phone.length() != 11) {
            return phone;
        }
        return phone.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2");
    }
}




