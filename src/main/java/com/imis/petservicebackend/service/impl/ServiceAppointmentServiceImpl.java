package com.imis.petservicebackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.imis.petservicebackend.common.BusinessException;
import com.imis.petservicebackend.entity.Pet;
import com.imis.petservicebackend.entity.PetServiceEntity;
import com.imis.petservicebackend.entity.ServiceAppointment;
import com.imis.petservicebackend.entity.User;
import com.imis.petservicebackend.service.PetQueryService;
import com.imis.petservicebackend.service.PetServiceQueryService;
import com.imis.petservicebackend.service.ServiceAppointmentService;
import com.imis.petservicebackend.service.UserService;
import com.imis.petservicebackend.mapper.ServiceAppointmentMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author 64360
 * @description 针对表【service_appointment(服务预约表)】的数据库操作Service实现
 * @createDate 2026-03-02 00:00:59
 */
@Service
@Slf4j
public class ServiceAppointmentServiceImpl extends ServiceImpl<ServiceAppointmentMapper, ServiceAppointment>
        implements ServiceAppointmentService {

    @Autowired
    private UserService userService;

    @Autowired
    private PetQueryService petQueryService;

    @Autowired
    private PetServiceQueryService petServiceQueryService;

    @Override
    public boolean createAppointment(Long userId, ServiceAppointment appointment) {
        // 校验宠物是否属于当前用户
        Pet pet = petQueryService.getById(appointment.getPetId());
        if (pet == null) {
            throw new BusinessException("宠物不存在");
        }
        if (!userId.equals(pet.getOwnerId())) {
            throw new BusinessException("只能为自己的宠物预约服务");
        }
        // 校验服务是否存在且启用
        PetServiceEntity service = petServiceQueryService.getById(appointment.getServiceId());
        if (service == null || service.getStatus() != 1) {
            throw new BusinessException("服务不存在或已禁用");
        }
        appointment.setUserId(userId);
        appointment.setStatus(1); // 已预约
        return this.save(appointment);
    }

    @Override
    public Page<Map<String, Object>> getMyAppointmentPage(Long userId, Integer status,
            Integer page, Integer pageSize) {
        Page<ServiceAppointment> pageInfo = new Page<>(page, pageSize);
        LambdaQueryWrapper<ServiceAppointment> queryWrapper = new LambdaQueryWrapper<>();

        queryWrapper.eq(ServiceAppointment::getUserId, userId)
                .eq(status != null, ServiceAppointment::getStatus, status)
                .orderByDesc(ServiceAppointment::getCreateTime);

        Page<ServiceAppointment> appointmentPage = this.page(pageInfo, queryWrapper);

        // 转换为 Map，携带关联信息
        Page<Map<String, Object>> resultPage = new Page<>(page, pageSize);
        resultPage.setTotal(appointmentPage.getTotal());

        List<Map<String, Object>> records = appointmentPage.getRecords().stream().map(appointment -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", appointment.getId());
            map.put("userId", appointment.getUserId());
            map.put("petId", appointment.getPetId());
            map.put("serviceId", appointment.getServiceId());
            map.put("appointmentTime", appointment.getAppointmentTime());
            map.put("status", appointment.getStatus());
            map.put("remark", appointment.getRemark());
            map.put("createTime", appointment.getCreateTime());

            // 获取宠物名
            Pet pet = petQueryService.getById(appointment.getPetId());
            map.put("petName", pet != null ? pet.getName() : "未知宠物");
            map.put("petImage", pet != null ? pet.getImage() : null);

            // 获取服务名和价格
            PetServiceEntity service = petServiceQueryService.getById(appointment.getServiceId());
            map.put("serviceName", service != null ? service.getName() : "未知服务");
            map.put("servicePrice", service != null ? service.getPrice() : null);

            return map;
        }).collect(Collectors.toList());

        resultPage.setRecords(records);
        return resultPage;
    }

    @Override
    public Map<String, Object> getAppointmentDetail(Long id) {
        ServiceAppointment appointment = this.getById(id);
        if (appointment == null) {
            return null;
        }

        Map<String, Object> map = new HashMap<>();
        map.put("id", appointment.getId());
        map.put("userId", appointment.getUserId());
        map.put("petId", appointment.getPetId());
        map.put("serviceId", appointment.getServiceId());
        map.put("appointmentTime", appointment.getAppointmentTime());
        map.put("status", appointment.getStatus());
        map.put("remark", appointment.getRemark());
        map.put("createTime", appointment.getCreateTime());

        // 获取用户信息
        User user = userService.getById(appointment.getUserId());
        if (user != null) {
            map.put("username", user.getUsername());
        }

        // 获取宠物信息
        Pet pet = petQueryService.getById(appointment.getPetId());
        if (pet != null) {
            map.put("petName", pet.getName());
            map.put("petImage", pet.getImage());
            map.put("petType", pet.getType());
            map.put("petBreed", pet.getBreed());
        }

        // 获取服务信息
        PetServiceEntity service = petServiceQueryService.getById(appointment.getServiceId());
        if (service != null) {
            map.put("serviceName", service.getName());
            map.put("serviceType", service.getType());
            map.put("servicePrice", service.getPrice());
            map.put("serviceDuration", service.getDuration());
        }

        return map;
    }

    @Override
    public boolean cancelAppointment(Long userId, Long id) {
        ServiceAppointment appointment = this.getById(id);
        if (appointment == null) {
            throw new BusinessException("预约记录不存在");
        }
        if (!userId.equals(appointment.getUserId())) {
            throw new BusinessException("无权操作他人的预约");
        }
        if (appointment.getStatus() != 1) {
            throw new BusinessException("只能取消状态为'已预约'的记录");
        }
        LambdaUpdateWrapper<ServiceAppointment> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ServiceAppointment::getId, id)
                .set(ServiceAppointment::getStatus, 3); // 3-已取消
        return this.update(updateWrapper);
    }
}
