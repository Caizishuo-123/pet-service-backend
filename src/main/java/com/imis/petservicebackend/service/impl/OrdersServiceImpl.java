package com.imis.petservicebackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.imis.petservicebackend.common.BusinessException;
import com.imis.petservicebackend.entity.AdoptionApply;
import com.imis.petservicebackend.entity.Orders;
import com.imis.petservicebackend.entity.Pet;
import com.imis.petservicebackend.entity.ServiceAppointment;
import com.imis.petservicebackend.mapper.AdoptionApplyMapper;
import com.imis.petservicebackend.service.OrdersService;
import com.imis.petservicebackend.service.PetQueryService;
import com.imis.petservicebackend.service.ServiceAppointmentService;
import com.imis.petservicebackend.mapper.OrdersMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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

    @Autowired
    private AdoptionApplyMapper adoptionApplyMapper;

    @Autowired
    private PetQueryService petQueryService;

    @Override
    public Page<Map<String, Object>> getMyOrderPage(Long userId, Integer payStatus, Integer orderType,
            String keyword, Integer page, Integer pageSize) {
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();

        queryWrapper.eq(Orders::getUserId, userId)
                .eq(payStatus != null, Orders::getPayStatus, payStatus)
                .eq(orderType != null, Orders::getOrderType, orderType)
                .orderByDesc(Orders::getCreateTime);

        Page<Map<String, Object>> resultPage = new Page<>(page, pageSize);
        List<Orders> orderList = this.list(queryWrapper);

        List<Map<String, Object>> matchedRecords = orderList.stream().map(order -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", order.getId());
            map.put("orderNo", order.getOrderNo());
            map.put("userId", order.getUserId());
            map.put("orderType", order.getOrderType());
            map.put("relatedId", order.getRelatedId());
            map.put("totalPrice", order.getTotalPrice());
            map.put("payStatus", order.getPayStatus());
            map.put("payTime", order.getPayTime());
            map.put("payMethod", order.getPayMethod());
            map.put("remark", order.getRemark());
            map.put("createTime", order.getCreateTime());
            fillOrderBusinessInfo(map, order);
            return map;
        }).filter(map -> matchesKeyword(map, keyword)).collect(Collectors.toList());

        resultPage.setTotal(matchedRecords.size());
        long fromIndex = Math.max(0L, (long) (page - 1) * pageSize);
        long toIndex = Math.min(fromIndex + pageSize, matchedRecords.size());
        if (fromIndex >= matchedRecords.size()) {
            resultPage.setRecords(Collections.emptyList());
        } else {
            resultPage.setRecords(new ArrayList<>(matchedRecords.subList((int) fromIndex, (int) toIndex)));
        }
        return resultPage;
    }

    @Override
    public Map<String, Object> getOrderDetail(Long userId, Long id) {
        Orders order = this.getById(id);
        if (order == null) {
            return null;
        }
        if (!Objects.equals(order.getUserId(), userId)) {
            throw new BusinessException("无权查看他人的订单");
        }

        Map<String, Object> map = new HashMap<>();
        map.put("id", order.getId());
        map.put("orderNo", order.getOrderNo());
        map.put("userId", order.getUserId());
        map.put("orderType", order.getOrderType());
        map.put("relatedId", order.getRelatedId());
        map.put("totalPrice", order.getTotalPrice());
        map.put("payStatus", order.getPayStatus());
        map.put("payTime", order.getPayTime());
        map.put("payMethod", order.getPayMethod());
        map.put("remark", order.getRemark());
        map.put("createTime", order.getCreateTime());
        fillOrderBusinessInfo(map, order);
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
        if (!isValidOrderType(order.getOrderType())) {
            throw new BusinessException("订单类型不存在");
        }
        if (order.getOrderType() == ORDER_TYPE_APPOINTMENT) {
            ServiceAppointment appointment = serviceAppointmentService.getById(order.getRelatedId());
            if (appointment == null) {
                throw new BusinessException("关联预约不存在，无法支付");
            }
            if (!Objects.equals(appointment.getStatus(), 1)) {
                throw new BusinessException("当前预约状态为" + appointmentStatusText(appointment.getStatus()) + "，不能继续支付");
            }
        }
        if (order.getOrderType() == ORDER_TYPE_ADOPTION) {
            AdoptionApply apply = adoptionApplyMapper.selectById(order.getRelatedId());
            if (apply == null) {
                throw new BusinessException("关联领养申请不存在，无法支付");
            }
            if (!Objects.equals(apply.getStatus(), 2)) {
                throw new BusinessException("当前领养申请状态为" + adoptionStatusText(apply.getStatus()) + "，不能继续支付");
            }
        }
        LambdaUpdateWrapper<Orders> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Orders::getId, id)
                .set(Orders::getPayStatus, 1)
                .set(Orders::getPayMethod, payMethod)
                .set(Orders::getPayTime, new Date());
        boolean updated = this.update(updateWrapper);
        if (updated && order.getOrderType() == ORDER_TYPE_ADOPTION) {
            updateAdoptionApplyStatus(order.getRelatedId(), 4);
        }
        return updated;
    }

    @Override
    public String generateOrderNo() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String dateStr = sdf.format(new Date());
        String random = String.format("%04d", new Random().nextInt(10000));
        return "PO" + dateStr + random;
    }

    @Override
    public Orders createAppointmentOrder(Long userId, Long appointmentId, BigDecimal totalPrice,
            String remark) {
        Orders existOrder = this.getOne(new LambdaQueryWrapper<Orders>()
                .eq(Orders::getOrderType, ORDER_TYPE_APPOINTMENT)
                .eq(Orders::getRelatedId, appointmentId)
                .last("limit 1"));
        if (existOrder != null) {
            return existOrder;
        }
        Orders order = new Orders();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setOrderType(ORDER_TYPE_APPOINTMENT);
        order.setRelatedId(appointmentId);
        order.setTotalPrice(totalPrice);
        order.setPayStatus(0);
        order.setRemark(remark);
        order.setCreateTime(new Date());
        if (!this.save(order)) {
            throw new BusinessException("创建订单失败");
        }
        return order;
    }

    private void fillOrderBusinessInfo(Map<String, Object> map, Orders order) {
        map.put("canPay", false);
        map.put("cannotPayReason", null);
        map.put("businessStatus", null);
        map.put("businessStatusText", "处理中");

        if (order.getOrderType() == ORDER_TYPE_APPOINTMENT) {
            map.put("orderTypeName", "服务预约");
            map.put("businessName", "服务预约");
            Map<String, Object> appointmentDetail = serviceAppointmentService
                    .getAppointmentDetail(order.getRelatedId());
            if (appointmentDetail != null) {
                map.put("petName", appointmentDetail.get("petName"));
                map.put("petImage", appointmentDetail.get("petImage"));
                map.put("serviceName", appointmentDetail.get("serviceName"));
                map.put("serviceType", appointmentDetail.get("serviceType"));
                map.put("servicePrice", appointmentDetail.get("servicePrice"));
                map.put("appointmentTime", appointmentDetail.get("appointmentTime"));
                map.put("appointmentStatus", appointmentDetail.get("status"));
                map.put("businessStatus", appointmentDetail.get("status"));
                map.put("businessStatusText", appointmentStatusText(toInteger(appointmentDetail.get("status"))));
                map.put("appointmentRemark", appointmentDetail.get("remark"));
                Integer appointmentStatus = toInteger(appointmentDetail.get("status"));
                boolean canPay = Objects.equals(order.getPayStatus(), 0) && Objects.equals(appointmentStatus, 1);
                map.put("canPay", canPay);
                if (!canPay && Objects.equals(order.getPayStatus(), 0)) {
                    map.put("cannotPayReason", buildAppointmentCannotPayReason(appointmentStatus));
                }
            } else if (Objects.equals(order.getPayStatus(), 0)) {
                map.put("cannotPayReason", "关联预约不存在");
            }
            return;
        }
        if (order.getOrderType() == ORDER_TYPE_ADOPTION) {
            map.put("orderTypeName", "领养支付");
            map.put("businessName", "宠物领养");
            map.put("serviceName", "宠物领养");
            AdoptionApply apply = adoptionApplyMapper.selectById(order.getRelatedId());
            if (apply == null) {
                if (Objects.equals(order.getPayStatus(), 0)) {
                    map.put("cannotPayReason", "关联领养申请不存在");
                }
                return;
            }
            map.put("adoptionApplyId", apply.getId());
            map.put("applyReason", apply.getApplyReason());
            map.put("deliveryType", apply.getDeliveryType());
            map.put("contactPhone", apply.getContactPhone());
            map.put("address", apply.getAddress());
            map.put("adoptionStatus", apply.getStatus());
            map.put("businessStatus", apply.getStatus());
            map.put("businessStatusText", adoptionStatusText(apply.getStatus()));
            boolean canPay = Objects.equals(order.getPayStatus(), 0) && Objects.equals(apply.getStatus(), 2);
            map.put("canPay", canPay);
            if (!canPay && Objects.equals(order.getPayStatus(), 0)) {
                map.put("cannotPayReason", buildAdoptionCannotPayReason(apply.getStatus()));
            }
            Pet pet = petQueryService.getById(apply.getPetId());
            if (pet != null) {
                map.put("petName", pet.getName());
                map.put("petImage", pet.getImage());
                map.put("petType", pet.getType());
                map.put("petBreed", pet.getBreed());
                map.put("adoptionFee", pet.getAdoptionFee());
            }
        }
    }

    private boolean matchesKeyword(Map<String, Object> map, String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return true;
        }
        String normalized = keyword.trim().toLowerCase(Locale.ROOT);
        return containsText(map.get("orderNo"), normalized)
                || containsText(map.get("petName"), normalized)
                || containsText(map.get("serviceName"), normalized)
                || containsText(map.get("businessName"), normalized);
    }

    private boolean containsText(Object value, String keyword) {
        return value != null && value.toString().toLowerCase(Locale.ROOT).contains(keyword);
    }

    private Integer toInteger(Object value) {
        if (value instanceof Integer) {
            return (Integer) value;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return null;
    }

    private String appointmentStatusText(Integer status) {
        if (Objects.equals(status, 1)) {
            return "已预约";
        }
        if (Objects.equals(status, 2)) {
            return "已完成";
        }
        if (Objects.equals(status, 3)) {
            return "已取消";
        }
        return "处理中";
    }

    private String adoptionStatusText(Integer status) {
        if (Objects.equals(status, 1)) {
            return "待审核";
        }
        if (Objects.equals(status, 2)) {
            return "待支付";
        }
        if (Objects.equals(status, 3)) {
            return "已拒绝";
        }
        if (Objects.equals(status, 4)) {
            return "已完成";
        }
        if (Objects.equals(status, 5)) {
            return "已取消";
        }
        return "处理中";
    }

    private String buildAppointmentCannotPayReason(Integer status) {
        if (Objects.equals(status, 3)) {
            return "预约已取消";
        }
        if (Objects.equals(status, 2)) {
            return "预约已完成";
        }
        return "当前预约不可支付";
    }

    private String buildAdoptionCannotPayReason(Integer status) {
        if (Objects.equals(status, 1)) {
            return "领养申请仍在审核中";
        }
        if (Objects.equals(status, 3)) {
            return "领养申请已被拒绝";
        }
        if (Objects.equals(status, 4)) {
            return "领养已完成";
        }
        if (Objects.equals(status, 5)) {
            return "领养申请已取消";
        }
        return "当前领养申请不可支付";
    }

    private void updateAdoptionApplyStatus(Long applyId, Integer status) {
        LambdaUpdateWrapper<AdoptionApply> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(AdoptionApply::getId, applyId)
                .set(AdoptionApply::getStatus, status);
        adoptionApplyMapper.update(null, updateWrapper);
        if (Objects.equals(status, 4)) {
            completeAdoption(applyId);
        }
    }

    private void completeAdoption(Long applyId) {
        AdoptionApply apply = adoptionApplyMapper.selectById(applyId);
        if (apply == null || apply.getPetId() == null || apply.getUserId() == null) {
            return;
        }
        Pet sourcePet = petQueryService.getById(apply.getPetId());
        if (sourcePet == null || Objects.equals(sourcePet.getStatus(), PetQueryService.STATUS_ADOPTION_FINISHED)) {
            return;
        }

        Pet sourceUpdate = new Pet();
        sourceUpdate.setId(sourcePet.getId());
        sourceUpdate.setStatus(PetQueryService.STATUS_ADOPTION_FINISHED);
        petQueryService.updateById(sourceUpdate);

        Pet adoptedPet = new Pet();
        adoptedPet.setName(sourcePet.getName());
        adoptedPet.setImage(sourcePet.getImage());
        adoptedPet.setType(sourcePet.getType());
        adoptedPet.setBreed(sourcePet.getBreed());
        adoptedPet.setAge(sourcePet.getAge());
        adoptedPet.setGender(sourcePet.getGender());
        adoptedPet.setHealthStatus(sourcePet.getHealthStatus());
        adoptedPet.setDescription(sourcePet.getDescription());
        adoptedPet.setAdoptionFee(BigDecimal.ZERO);
        adoptedPet.setSource(1);
        adoptedPet.setOwnerId(apply.getUserId());
        adoptedPet.setStatus(PetQueryService.STATUS_OWNED);
        petQueryService.save(adoptedPet);

        LambdaUpdateWrapper<AdoptionApply> rejectWrapper = new LambdaUpdateWrapper<>();
        rejectWrapper.eq(AdoptionApply::getPetId, apply.getPetId())
                .ne(AdoptionApply::getId, apply.getId())
                .eq(AdoptionApply::getStatus, 1)
                .set(AdoptionApply::getStatus, 3);
        adoptionApplyMapper.update(null, rejectWrapper);
    }

    private boolean isValidOrderType(Integer orderType) {
        return orderType != null && (orderType == ORDER_TYPE_APPOINTMENT
                || orderType == ORDER_TYPE_ADOPTION);
    }
}
