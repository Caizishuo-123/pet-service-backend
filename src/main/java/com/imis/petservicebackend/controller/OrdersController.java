package com.imis.petservicebackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.imis.petservicebackend.common.BusinessException;
import com.imis.petservicebackend.common.Result;
import com.imis.petservicebackend.entity.User;
import com.imis.petservicebackend.service.OrdersService;
import com.imis.petservicebackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/orders")
@Slf4j
public class OrdersController {

  @Autowired
  private OrdersService ordersService;

  @Autowired
  private UserService userService;

  /**
   * 我的订单列表
   */
  @GetMapping("/mine")
  public Result<?> getMyOrders(
      @RequestAttribute("account") String account,
      @RequestParam(required = false) Integer payStatus,
      @RequestParam(defaultValue = "1") Integer page,
      @RequestParam(defaultValue = "10") Integer pageSize) {
    Long userId = getUserId(account);
    Page<Map<String, Object>> pageInfo = ordersService
        .getMyOrderPage(userId, payStatus, page, pageSize);
    return Result.success(pageInfo);
  }

  /**
   * 订单详情
   */
  @GetMapping("/detail/{id}")
  public Result<?> getOrderDetail(@RequestAttribute("account") String account,
      @PathVariable Long id) {
    Map<String, Object> detail = ordersService.getOrderDetail(id);
    if (detail == null) {
      return Result.fail("订单不存在");
    }
    return Result.success(detail);
  }

  /**
   * 模拟支付
   *
   * @param id        订单ID
   * @param payMethod 支付方式：1-微信 2-支付宝
   */
  @PutMapping("/pay")
  public Result<?> simulatePay(@RequestAttribute("account") String account,
      @RequestParam Long id,
      @RequestParam Integer payMethod) {
    Long userId = getUserId(account);
    if (payMethod != 1 && payMethod != 2) {
      return Result.fail("支付方式无效，只能是1(微信)或2(支付宝)");
    }
    boolean flag = ordersService.simulatePay(userId, id, payMethod);
    return flag ? Result.success("支付成功") : Result.fail("支付失败");
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
