package com.imis.petservicebackend.service;

import com.imis.petservicebackend.entity.Orders;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

/**
 * @author 64360
 * @description 针对表【orders(订单表)】的数据库操作Service
 * @createDate 2026-03-02 00:01:13
 */
public interface OrdersService extends IService<Orders> {

  /**
   * 我的订单列表（分页，带关联信息）
   */
  Page<Map<String, Object>> getMyOrderPage(Long userId, Integer payStatus,
      Integer page, Integer pageSize);

  /**
   * 订单详情
   */
  Map<String, Object> getOrderDetail(Long id);

  /**
   * 模拟支付（更新支付状态）
   */
  boolean simulatePay(Long userId, Long id, Integer payMethod);

  /**
   * 生成订单号
   */
  String generateOrderNo();
}
