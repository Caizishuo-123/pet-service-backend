package com.imis.petservicebackend.controller;

import com.imis.petservicebackend.common.Result;
import com.imis.petservicebackend.entity.LoginVO;
import com.imis.petservicebackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public Result<?> login(@RequestParam("account") String account, @RequestParam("password") String password) {
        LoginVO vo = userService.login(account , password);
        return Result.success(vo);
    }
}
