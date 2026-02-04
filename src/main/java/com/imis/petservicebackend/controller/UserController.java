package com.imis.petservicebackend.controller;

import com.imis.petservicebackend.common.Result;
import com.imis.petservicebackend.entity.User;
import com.imis.petservicebackend.entity.UserInfo;
import com.imis.petservicebackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {
    @Autowired
    private UserService userService;

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

    @PostMapping("verifyPhone")
    public Result<?> verifyPhone(@RequestParam("userName") String userName, @RequestParam("phone") String phone) {
        boolean result = userService.verifyPhone(userName, phone);
        return Result.success(result);
    }

    @PutMapping("updatePassword")
    public Result<?> updatePassword(@RequestParam("userName") String userName, @RequestParam("password") String password, @RequestParam("repeatPassword") String repeatPassword) {
        boolean result = userService.updatePassword(userName, password, repeatPassword);
        return Result.success(result);
    }


}
