package com.imis.petservicebackend.service;

import com.imis.petservicebackend.entity.LoginVO;
import com.imis.petservicebackend.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author 64360
* @description 针对表【user(用户表)】的数据库操作Service
* @createDate 2026-02-02 15:22:54
*/
public interface UserService extends IService<User> {


    LoginVO login(String account, String password);
}
