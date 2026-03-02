package com.imis.petservicebackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.imis.petservicebackend.entity.PetServiceEntity;
import com.imis.petservicebackend.mapper.PetServiceMapper;
import com.imis.petservicebackend.service.PetServiceQueryService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @description 针对表【pet_service(宠物服务表)】的数据库操作Service实现（用户端，只读）
 */
@Service
public class PetServiceQueryServiceImpl extends ServiceImpl<PetServiceMapper, PetServiceEntity>
    implements PetServiceQueryService {

  @Override
  public Page<PetServiceEntity> getServicePage(Integer type, Integer page, Integer pageSize) {
    Page<PetServiceEntity> pageInfo = new Page<>(page, pageSize);
    LambdaQueryWrapper<PetServiceEntity> queryWrapper = new LambdaQueryWrapper<>();

    // 只查启用的服务
    queryWrapper.eq(PetServiceEntity::getStatus, 1)
        .eq(type != null, PetServiceEntity::getType, type)
        .orderByDesc(PetServiceEntity::getSort)
        .orderByDesc(PetServiceEntity::getCreateTime);

    return this.page(pageInfo, queryWrapper);
  }

  @Override
  public List<PetServiceEntity> getEnabledServiceList() {
    LambdaQueryWrapper<PetServiceEntity> queryWrapper = new LambdaQueryWrapper<>();
    queryWrapper.eq(PetServiceEntity::getStatus, 1)
        .orderByDesc(PetServiceEntity::getSort);
    return this.list(queryWrapper);
  }
}
