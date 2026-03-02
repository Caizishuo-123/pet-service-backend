package com.imis.petservicebackend.service;

import com.imis.petservicebackend.entity.AdoptionApply;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

/**
 * @author 64360
 * @description 针对表【adoption_apply(领养申请表)】的数据库操作Service
 * @createDate 2026-03-02 00:01:04
 */
public interface AdoptionApplyService extends IService<AdoptionApply> {

  /**
   * 提交领养申请
   */
  boolean submitApply(Long userId, AdoptionApply apply);

  /**
   * 我的申请列表（分页，带关联信息）
   */
  Page<Map<String, Object>> getMyApplyPage(Long userId, Integer status,
      Integer page, Integer pageSize);

  /**
   * 申请详情（带关联信息）
   */
  Map<String, Object> getApplyDetail(Long id);
}
