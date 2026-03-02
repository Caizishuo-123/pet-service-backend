package com.imis.petservicebackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.imis.petservicebackend.common.BusinessException;
import com.imis.petservicebackend.common.Result;
import com.imis.petservicebackend.entity.ServiceAppointment;
import com.imis.petservicebackend.entity.User;
import com.imis.petservicebackend.service.ServiceAppointmentService;
import com.imis.petservicebackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/appointment")
@Slf4j
public class AppointmentController {

  @Autowired
  private ServiceAppointmentService serviceAppointmentService;

  @Autowired
  private UserService userService;

  /**
   * 创建预约
   */
  @PostMapping("/add")
  public Result<?> createAppointment(@RequestAttribute("account") String account,
      @RequestBody ServiceAppointment appointment) {
    Long userId = getUserId(account);
    boolean flag = serviceAppointmentService.createAppointment(userId, appointment);
    return flag ? Result.success("预约成功") : Result.fail("预约失败");
  }

  /**
   * 我的预约列表
   */
  @GetMapping("/mine")
  public Result<?> getMyAppointments(
      @RequestAttribute("account") String account,
      @RequestParam(required = false) Integer status,
      @RequestParam(defaultValue = "1") Integer page,
      @RequestParam(defaultValue = "10") Integer pageSize) {
    Long userId = getUserId(account);
    Page<Map<String, Object>> pageInfo = serviceAppointmentService
        .getMyAppointmentPage(userId, status, page, pageSize);
    return Result.success(pageInfo);
  }

  /**
   * 预约详情
   */
  @GetMapping("/detail/{id}")
  public Result<?> getAppointmentDetail(@PathVariable Long id) {
    Map<String, Object> detail = serviceAppointmentService.getAppointmentDetail(id);
    if (detail == null) {
      return Result.fail("预约记录不存在");
    }
    return Result.success(detail);
  }

  /**
   * 取消预约（只能取消自己的，且状态为已预约）
   */
  @PutMapping("/cancel/{id}")
  public Result<?> cancelAppointment(@RequestAttribute("account") String account,
      @PathVariable Long id) {
    Long userId = getUserId(account);
    boolean flag = serviceAppointmentService.cancelAppointment(userId, id);
    return flag ? Result.success("取消成功") : Result.fail("取消失败");
  }

  /**
   * 根据用户名获取用户ID
   */
  private Long getUserId(String account) {
    LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(User::getUsername, account);
    User user = userService.getOne(wrapper);
    if (user == null) {
      throw new BusinessException("用户不存在");
    }
    return user.getId();
  }
}
