package com.imis.petservicebackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.imis.petservicebackend.common.BusinessException;
import com.imis.petservicebackend.entity.AdoptionApply;
import com.imis.petservicebackend.entity.Pet;
import com.imis.petservicebackend.service.AdoptionApplyService;
import com.imis.petservicebackend.service.PetQueryService;
import com.imis.petservicebackend.mapper.AdoptionApplyMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author 64360
 * @description 针对表【adoption_apply(领养申请表)】的数据库操作Service实现
 * @createDate 2026-03-02 00:01:04
 */
@Service
@Slf4j
public class AdoptionApplyServiceImpl extends ServiceImpl<AdoptionApplyMapper, AdoptionApply>
        implements AdoptionApplyService {

    @Autowired
    private PetQueryService petQueryService;

    @Override
    public boolean submitApply(Long userId, AdoptionApply apply) {
        // 校验宠物是否存在且状态为可领养
        Pet pet = petQueryService.getById(apply.getPetId());
        if (pet == null) {
            throw new BusinessException("宠物不存在");
        }
        if (pet.getStatus() != 2) {
            throw new BusinessException("该宠物当前不可领养");
        }
        // 检查是否已申请过该宠物（待审核状态）
        LambdaQueryWrapper<AdoptionApply> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(AdoptionApply::getUserId, userId)
                .eq(AdoptionApply::getPetId, apply.getPetId())
                .eq(AdoptionApply::getStatus, 1); // 待审核
        if (this.count(checkWrapper) > 0) {
            throw new BusinessException("您已申请过该宠物，请勿重复申请");
        }
        apply.setUserId(userId);
        apply.setStatus(1); // 待审核
        return this.save(apply);
    }

    @Override
    public Page<Map<String, Object>> getMyApplyPage(Long userId, Integer status,
            Integer page, Integer pageSize) {
        Page<AdoptionApply> pageInfo = new Page<>(page, pageSize);
        LambdaQueryWrapper<AdoptionApply> queryWrapper = new LambdaQueryWrapper<>();

        queryWrapper.eq(AdoptionApply::getUserId, userId)
                .eq(status != null, AdoptionApply::getStatus, status)
                .orderByDesc(AdoptionApply::getCreateTime);

        Page<AdoptionApply> applyPage = this.page(pageInfo, queryWrapper);

        // 转换为 Map，携带宠物信息
        Page<Map<String, Object>> resultPage = new Page<>(page, pageSize);
        resultPage.setTotal(applyPage.getTotal());

        List<Map<String, Object>> records = applyPage.getRecords().stream().map(apply -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", apply.getId());
            map.put("userId", apply.getUserId());
            map.put("petId", apply.getPetId());
            map.put("applyReason", apply.getApplyReason());
            map.put("deliveryType", apply.getDeliveryType());
            map.put("address", apply.getAddress());
            map.put("contactPhone", apply.getContactPhone());
            map.put("status", apply.getStatus());
            map.put("createTime", apply.getCreateTime());

            // 获取宠物信息
            Pet pet = petQueryService.getById(apply.getPetId());
            map.put("petName", pet != null ? pet.getName() : "未知宠物");
            map.put("petImage", pet != null ? pet.getImage() : null);
            map.put("petType", pet != null ? pet.getType() : null);
            map.put("petBreed", pet != null ? pet.getBreed() : null);

            return map;
        }).collect(Collectors.toList());

        resultPage.setRecords(records);
        return resultPage;
    }

    @Override
    public Map<String, Object> getApplyDetail(Long id) {
        AdoptionApply apply = this.getById(id);
        if (apply == null) {
            return null;
        }

        Map<String, Object> map = new HashMap<>();
        map.put("id", apply.getId());
        map.put("userId", apply.getUserId());
        map.put("petId", apply.getPetId());
        map.put("applyReason", apply.getApplyReason());
        map.put("deliveryType", apply.getDeliveryType());
        map.put("address", apply.getAddress());
        map.put("contactPhone", apply.getContactPhone());
        map.put("status", apply.getStatus());
        map.put("createTime", apply.getCreateTime());
        map.put("updateTime", apply.getUpdateTime());

        // 获取宠物详细信息
        Pet pet = petQueryService.getById(apply.getPetId());
        if (pet != null) {
            map.put("petName", pet.getName());
            map.put("petImage", pet.getImage());
            map.put("petType", pet.getType());
            map.put("petBreed", pet.getBreed());
            map.put("petAge", pet.getAge());
            map.put("petGender", pet.getGender());
            map.put("petDescription", pet.getDescription());
        }

        return map;
    }
}
