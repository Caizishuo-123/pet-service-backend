package com.imis.petservicebackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.imis.petservicebackend.common.BusinessException;
import com.imis.petservicebackend.entity.Orders;
import com.imis.petservicebackend.service.OrdersService;
import com.imis.petservicebackend.service.ServiceAppointmentService;
import com.imis.petservicebackend.mapper.OrdersMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author 64360
 * @description 针对表【orders(订单表)】的数据库操作Service实现
 * @createDate 2026-03-02 00:01:13
 */
@Service
@Slf4j
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper, Orders>
        implements OrdersService {

    @Autowired
    private ServiceAppointmentService serviceAppointmentService;

    @Override
    public Page<Map<String, Object>> getMyOrderPage(Long userId, Integer payStatus,
            Integer page, Integer pageSize) {
        Page<Orders> pageInfo = new Page<>(page, pageSize);
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();

        queryWrapper.eq(Orders::getUserId, userId)
                .eq(payStatus != null, Orders::getPayStatus, payStatus)
                .orderByDesc(Orders::getCreateTime);

        Page<Orders> ordersPage = this.page(pageInfo, queryWrapper);

        // 转换为 Map，携带关联信息
        Page<Map<String, Object>> resultPage = new Page<>(page, pageSize);
        resultPage.setTotal(ordersPage.getTotal());

        List<Map<String, Object>> records = ordersPage.getRecords().stream().map(order -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", order.getId());
            map.put("orderNo", order.getOrderNo());
            map.put("userId", order.getUserId());
            map.put("appointmentId", order.getAppointmentId());
            map.put("totalPrice", order.getTotalPrice());
            map.put("payStatus", order.getPayStatus());
            map.put("payTime", order.getPayTime());
            map.put("payMethod", order.getPayMethod());
            map.put("remark", order.getRemark());
            map.put("createTime", order.getCreateTime());

            // 获取预约关联信息（服务名、宠物名等）
            Map<String, Object> appointmentDetail = serviceAppointmentService
                    .getAppointmentDetail(order.getAppointmentId());
            if (appointmentDetail != null) {
                map.put("petName", appointmentDetail.get("petName"));
                map.put("serviceName", appointmentDetail.get("serviceName"));
                map.put("appointmentTime", appointmentDetail.get("appointmentTime"));
            }

            return map;
        }).collect(Collectors.toList());

        resultPage.setRecords(records);
        return resultPage;
    }

    @Override
    public Map<String, Object> getOrderDetail(Long id) {
        Orders order = this.getById(id);
        if (order == null) {
            return null;
        }

        Map<String, Object> map = new HashMap<>();
        map.put("id", order.getId());
        map.put("orderNo", order.getOrderNo());
        map.put("userId", order.getUserId());
        map.put("appointmentId", order.getAppointmentId());
        map.put("totalPrice", order.getTotalPrice());
        map.put("payStatus", order.getPayStatus());
        map.put("payTime", order.getPayTime());
        map.put("payMethod", order.getPayMethod());
        map.put("remark", order.getRemark());
        map.put("createTime", order.getCreateTime());

        // 获取预约详情（含宠物、服务信息）
        Map<String, Object> appointmentDetail = serviceAppointmentService
                .getAppointmentDetail(order.getAppointmentId());
        if (appointmentDetail != null) {
            map.put("petName", appointmentDetail.get("petName"));
            map.put("petImage", appointmentDetail.get("petImage"));
            map.put("serviceName", appointmentDetail.get("serviceName"));
            map.put("serviceType", appointmentDetail.get("serviceType"));
            map.put("servicePrice", appointmentDetail.get("servicePrice"));
            map.put("appointmentTime", appointmentDetail.get("appointmentTime"));
        }

        return map;
    }

    @Override
    public boolean simulatePay(Long userId, Long id, Integer payMethod) {
        Orders order = this.getById(id);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!userId.equals(order.getUserId())) {
            throw new BusinessException("无权操作他人的订单");
        }
        if (order.getPayStatus() == 1) {
            throw new BusinessException("订单已支付，请勿重复支付");
        }
        LambdaUpdateWrapper<Orders> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Orders::getId, id)
                .set(Orders::getPayStatus, 1)
                .set(Orders::getPayMethod, payMethod)
                .set(Orders::getPayTime, new Date());
        return this.update(updateWrapper);
    }

    @Override
    public String generateOrderNo() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String dateStr = sdf.format(new Date());
        String random = String.format("%04d", new Random().nextInt(10000));
        return "PO" + dateStr + random;
    }
}
