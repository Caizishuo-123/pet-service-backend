package com.imis.petservicebackend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.imis.petservicebackend.entity.PetServiceEntity;

import java.util.List;

/**
 * @description 针对表【pet_service(宠物服务表)】的数据库操作Service（用户端，只读）
 */
public interface PetServiceQueryService extends IService<PetServiceEntity> {

  /**
   * 分页查询启用的服务列表
   */
  Page<PetServiceEntity> getServicePage(Integer type, Integer page, Integer pageSize);

  /**
   * 获取所有启用的服务（不分页，供下拉选择）
   */
  List<PetServiceEntity> getEnabledServiceList();
}
